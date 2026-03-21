import React, { useState, useEffect } from 'react';
import { createRoot } from 'react-dom/client';

function App() {
    const [rooms, setRooms] = useState([]);
    const [messages, setMessages] = useState([]);
    const [activeRoom, setActiveRoom] = useState('Головна');
    const [newRoomName, setNewRoomName] = useState('');
    const [newMessageText, setNewMessageText] = useState('');
    const [search, setSearch] = useState('');
    const [sort, setSort] = useState('oldest');
    const [myName] = useState(() => prompt("Введіть ваше ім'я:") || "Анонім");

    const fetchRooms = () => fetch('/api/rooms').then(r => r.json()).then(setRooms);

    const fetchMessages = () => {
        fetch(`/api/messages?room=${activeRoom}&search=${search}&sort=${sort}`)
            .then(r => r.json())
            .then(setMessages);
    };

    useEffect(() => {
        fetchRooms();
        fetchMessages();

        const timerId = setInterval(() => { fetchMessages() }, 3000);

        return () => clearInterval(timerId);
    }, [activeRoom, search, sort]);

    const createRoom = () => {
        if (!newRoomName) return;
        fetch('/api/rooms', {
            method: 'POST',
            body: JSON.stringify({ name: newRoomName })
        }).then(() => { setNewRoomName(''); fetchRooms(); });
    };

    const sendMessage = () => {
        if (!newMessageText) return;
        fetch('/api/messages', {
            method: 'POST',
            body: JSON.stringify({ room: activeRoom, text: newMessageText, sender: myName })
        }).then(() => { setNewMessageText(''); fetchMessages(); });
    };

    return (
        <div style={{ padding: '20px', fontFamily: 'sans-serif' }}>
            <h3>Кімнати:</h3>
            <input value={newRoomName} onChange={e => setNewRoomName(e.target.value)} placeholder="Нова кімната..." />
            <button onClick={createRoom}>Створити</button>
            <ul>
                {rooms.map(room => (
                    <li key={room}>
                        <button onClick={() => setActiveRoom(room)}>
                            {room === activeRoom ? '👉 ' : ''}{room}
                        </button>
                    </li>
                ))}
            </ul>
            <hr />

            <input value={search} onChange={e => setSearch(e.target.value)} placeholder="Пошук..." />
            <select value={sort} onChange={e => setSort(e.target.value)}>
                <option value="oldest">Спочатку старі</option>
                <option value="newest">Спочатку нові</option>
            </select>
            <br /><br />

            <ul>
                {messages.map((msg, idx) => <li key={idx}><b>{msg.sender}:</b> {msg.text}</li>)}
            </ul>

            <input
                value={newMessageText}
                onChange={e => setNewMessageText(e.target.value)}
                placeholder="Повідомлення..."
                onKeyDown={e => e.key === 'Enter' && sendMessage()}
            />
            <button onClick={sendMessage}>Відправити</button>
        </div>
    );
}

const root = createRoot(document.getElementById('root'));
root.render(<App />);

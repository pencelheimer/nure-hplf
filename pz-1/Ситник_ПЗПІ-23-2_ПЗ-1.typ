#import "@local/nure:0.1.1": pz-lb, style.appendices, style.spacing

#show: pz-lb.with(
  title: "Основи Python та робота з даними",
  subject: "ВМПтФ",
  layout: "ХНУРЕ",
  type: "ПЗ",
  number: 1,
  edu-program: "ПЗПІ",
  university: "ХНУРЕ",
  mentors: (
    (
      name: "Саманцов О. О.",
      degree: "ст. викл. кафедри ПІ",
      gender: "m",
    ),
  ),
  authors: (
    (
      name: "Ситник Є. С.",
      edu-program: "ПЗПІ",
      group: "23-2",
      gender: "m",
      variant: 12,
    ),
  ),
)

#v(-spacing)
== Мета роботи
Метою даної роботи є ознайомлення з базовими можливостями мови Python. Розгляд
базових лінійних конструкції, розгалужень, циклів, функцій, створення та
використання класів.

== Індивідуальне завдання
- Рівень 1: Напишіть функцію, яка приймає три параметри (a, b, c) і виводить на
  екран найменше з них.
- Рівень 2: Напишіть функцію, яка приймає рядок та повертає його обернений
  варіант. Наприклад, "hello" повинно повернути "olleh".
- Рівень 3: Реалізуйте програму, яка визначає, чи є слово паліндромом
  (читається однаково з обох боків).
- Рівень 4: Реалізуйте програму, яка визначає, чи є слово паліндромом
  (читається однаково з обох боків).


== Хід роботи

=== Рівень 1.
```python
def min_of_three[T: (int, float, str)](a: T, b: T, c: T) -> T:
    v1 = a if a < b else b
    v2 = v1 if v1 < c else c
    return v2
```

=== Рівень 2.
```python
def reverce_str(s: str) -> str:
    return s[::-1]
```

=== Рівень 3.
```python
def is_palindrome(s: str) -> bool:
    for i in range(len(s) // 2):
        if s[i] != s[-(i + 1)]:
            return False
    return True
```

=== Рівень 4.
```python
from typing import Any

def quicksort[T: Any](arr: list[T]) -> list[T]:
    if len(arr) <= 1:
        return arr

    pivot = arr[len(arr) // 2]

    left = [x for x in arr if x < pivot]
    middle = [x for x in arr if x == pivot]
    right = [x for x in arr if x > pivot]

    return quicksort(left) + middle + quicksort(right)
```

=== Код перевірки роботи розроблених функцій
```python
if __name__ == "__main__":
    assert min_of_three(7, 5, 10) == 5

    assert reverce_str("hello") == "olleh"

    assert not is_palindrome("hello")
    assert is_palindrome("olhlo")
    assert is_palindrome("ollo")

    assert quicksort([]) == []
    assert quicksort([1]) == [1]
    assert quicksort([2, 1]) == [1, 2]
    assert quicksort([3, 6, 8, 10, 1, 2]) == [1, 2, 3, 6, 8, 10]
```

Програма виконується без помилок.

== Висновки
Під час виконання даної роботи було розглянуто базові лінійні конструкції,
розгалуження, цикли, функції, створення та використання класів.

#show: appendices
= Повний код розробленої програми
```python
from typing import Any


def min_of_three[T: (int, float, str)](a: T, b: T, c: T) -> T:
    v1 = a if a < b else b
    v2 = v1 if v1 < c else c
    return v2


def reverce_str(s: str) -> str:
    return s[::-1]


def is_palindrome(s: str) -> bool:
    for i in range(len(s) // 2):
        if s[i] != s[-(i + 1)]:
            return False
    return True


def quicksort[T: Any](arr: list[T]) -> list[T]:
    if len(arr) <= 1:
        return arr

    pivot = arr[len(arr) // 2]

    left = [x for x in arr if x < pivot]
    middle = [x for x in arr if x == pivot]
    right = [x for x in arr if x > pivot]

    return quicksort(left) + middle + quicksort(right)


if __name__ == "__main__":
    assert min_of_three(7, 5, 10) == 5

    assert reverce_str("hello") == "olleh"

    assert not is_palindrome("hello")
    assert is_palindrome("olhlo")
    assert is_palindrome("ollo")

    assert quicksort([]) == []
    assert quicksort([1]) == [1]
    assert quicksort([2, 1]) == [1, 2]
    assert quicksort([3, 6, 8, 10, 1, 2]) == [1, 2, 3, 6, 8, 10]
```


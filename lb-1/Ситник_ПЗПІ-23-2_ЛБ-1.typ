#import "@local/nure:0.1.1": pz-lb, style.appendices, style.spacing

#show: pz-lb.with(
  title: "Django",
  subject: "ВМПтФ",
  layout: "ХНУРЕ",
  type: "ЛБ",
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
Метою даної роботи є ознайомлення з основами розробки за допомогою
веб-фреймворку Python Django. Розгляд архітектурного патерну MVC, проектування
моделей даних, написання контролерів для обробки запитів та використання мови
шаблонів для відображення інформації користувачу.

== Індивідуальне завдання
- Рівень 1: Створіть Django-модель "Категорія" з полями "Назва" та "Опис".
  Забезпечте можливість призначення статті до конкретної категорії.
- Рівень 2: Створіть Django-модель "Коментар" з полями "Автор", "Текст" та
  "Дата коментування". Забезпечте можливість додавання коментарів до статей.
- Рівень 3: Додайте пагінацію для списку статей, щоб користувач міг легко
  переглядати багато статей на одній сторінці.
- Рівень 4: Реалізуйте можливість пошуку статей за ключовими словами. Додайте
  функціонал пошуку до веб-інтерфейсу.

== Хід роботи

=== Рівень 1.
```python
@final
class Category(m.Model):
    name = m.CharField(max_length=255, verbose_name="Назва")
    description = m.TextField(blank=True, verbose_name="Опис")

    @override
    def __str__(self):
        return self.name
```

=== Рівень 2.
```python
@final
class Comment(m.Model):
    post = m.ForeignKey(
        Post,
        on_delete=m.CASCADE,
        related_name="comments",
        verbose_name="Стаття",
    )
    author = m.ForeignKey(User, on_delete=m.CASCADE, verbose_name="Автор")
    text = m.TextField(verbose_name="Текст коментаря")
    created_at = m.DateTimeField(auto_now_add=True, verbose_name="Дата коментування")

    @override
    def __str__(self):
        return f"Коментар від {self.author} до {self.post.title}"
```

=== Рівні 3 та 4.
```python
def article_list(request: HttpRequest) -> HttpResponse:
    search_query = request.GET.get("q", "")
    posts = Post.objects.all().order_by("-published")

    if search_query:
        posts = posts.filter(
            Q(title__icontains=search_query) | Q(content__icontains=search_query)
        )

    paginator = Paginator(posts, 5)
    page_number = request.GET.get("page")
    page_obj = paginator.get_page(page_number)

    context = {
        "page_obj": page_obj,
        "search_query": search_query,
    }

    return render(request, "blog/article_list.html", context)
```

== Висновки
Під час виконання даної роботи було розглянуто архітектурний патерну MVC,
спроектовано моделі даних, написано контролер для обробки запитів та
використано мову шаблонів для відображення інформації користувачу.

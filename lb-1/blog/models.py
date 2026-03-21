from typing import final, override
from django.db import models as m
from django.contrib.auth.models import User


@final
class Category(m.Model):
    name = m.CharField(max_length=255, verbose_name="Назва")
    description = m.TextField(blank=True, verbose_name="Опис")

    @override
    def __str__(self):
        return self.name


@final
class Post(m.Model):
    title = m.CharField(max_length=255, verbose_name="Заголовок")
    content = m.TextField(verbose_name="Текст")
    published = m.DateTimeField(auto_now_add=True, verbose_name="Дата публікації")
    category = m.ForeignKey(
        Category,
        on_delete=m.SET_NULL,
        null=True,
        related_name="posts",
        verbose_name="Категория",
    )

    @override
    def __str__(self):
        return self.title


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

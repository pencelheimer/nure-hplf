import os
import django

_ = os.environ.setdefault("DJANGO_SETTINGS_MODULE", "config.settings")
django.setup()

from django.contrib.auth.models import User
from blog.models import Category, Post, Comment


def seed_database() -> None:
    cat_tech, _ = Category.objects.get_or_create(
        name="Технології", description="Новини зі світу ІТ"
    )
    cat_science, _ = Category.objects.get_or_create(
        name="Наука", description="Нові відкриття"
    )

    user, _ = User.objects.get_or_create(username="test_commenter")
    if user.password == "":
        user.set_password("password123")
        user.save()

    for i in range(1, 13):
        category = cat_tech if i % 2 == 0 else cat_science

        post = Post.objects.create(
            title=f"Тестова стаття #{i} про Django",
            content=f"Це автоматично згенерований текст для статті номер {i}. Тут є ключові слова для перевірки пошуку.",
            category=category,
        )

        _comment = Comment.objects.create(
            post=post,
            author=user,
            text=f"Це тестовий коментар до статті {i}.",
        )


if __name__ == "__main__":
    seed_database()

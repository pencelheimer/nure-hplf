from typing import final
from django.contrib import admin
from .models import Category, Post, Comment

admin.site.register(Category)
admin.site.register(Comment)


@final
@admin.register(Post)
class PostAdmin(admin.ModelAdmin):  # pyright: ignore[reportMissingTypeArgument]
    list_display = ("title", "category", "published")
    search_fields = ("title", "content")
    list_filter = ("category",)

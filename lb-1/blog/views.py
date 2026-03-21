from django.shortcuts import render
from django.core.paginator import Paginator
from django.db.models import Q
from django.http import HttpRequest, HttpResponse
from .models import Post


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

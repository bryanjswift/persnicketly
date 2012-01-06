function map() {
  var a = {
    article_id: this.article_id,
    article_title: this.article_title,
    article_domain: this.article_domain,
    article_url: this.article_url,
    article_excerpt: this.article_excerpt,
    article_processed: this.article_processed,
    favorite_count: this.favorite ? 1 : 0,
    count: 1
  };
  emit(this.article_id, a);
}


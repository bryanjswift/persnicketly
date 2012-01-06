function reduce(key, values) {
  var result = {
    article_id: '',
    article_title: '',
    article_domain: '',
    article_url: '',
    article_excerpt: '',
    article_processed: false,
    favorite_count: 0,
    count: 0,
    article_id: key
  };
  values.forEach(function (a) {
    result.article_title = a.article_title;
    result.article_domain = a.article_domain;
    result.article_url = a.article_url;
    result.article_excerpt = a.article_excerpt;
    result.favorite_count += a.favorite_count;
    result.count += a.count;
  });
  return result;
}


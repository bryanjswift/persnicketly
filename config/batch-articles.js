db.articles.drop();
db.createCollection('articles');
db.articles.ensureIndex({ 'article_id': 1 }, { unique: true });
var marks = db.bookmarks.find();
marks.forEach(function(mark) {
  var q = { 'article_id': mark.article_id };
  var o = {
    'article_id': mark.article_id,
    'article_title': mark.article_title,
    'article_domain': mark.article_domain,
    'article_url': mark.article_url,
    'article_excerpt': mark.article_excerpt,
    'article_processed': mark.article_processed
  };
  db.articles.update(q, o, true, false);
})

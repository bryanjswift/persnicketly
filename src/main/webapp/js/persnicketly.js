(function() {
  var win = $(window)
    , t = false;

  $.domReady(function onDomReady() {
    resetAndResizeArticles();
    ajaxifyLinks();
  });
  win.addListener('resize', function onResize() {
    if (t) { clearTimeout(t); }
    t = setTimeout(function resizeTimeout() {
      win.trigger('resized');
      t = false;
    }, 100);
  });
  win.addListener('resized', function onResized() {
    resetAndResizeArticles();
    resetAndResizeContent();
  });
  win.addListener('load', resizeContent);

  function ajaxifyLinks() {
    $('.list li a.corner').bind('click', function cornerClick(e) {
      e.preventDefault();
      var el = $(this)
        , href = el.attr('href');
      reqwest({
        url: href,
        type: 'json',
        success: function cornerClickSuccess(res) {
          if (el.hasClass('add')) {
            el.addClass('star').removeClass('add');
          } else if (el.hasClass('star')) {
            el.addClass('starred').removeClass('star');
            el.attr('href', href.replace(/\bstar\b/, 'unstar'));
          } else if (el.hasClass('starred')) {
            el.addClass('star').removeClass('starred');
            el.attr('href', href.replace(/\bunstar\b/, 'star'));
          }
        }
      });
    });
  }
  function resetArticles() {
    $('.list li').attr('style', '');
  }
  function resizeArticles() {
    $('.list li').forEach(function(a) {
      var el = $(a)
        , neighbor = el.next()
        , elH = el.height()
        , neighborH = neighbor.height()
        , h;
      if (!el.hasClass('odd')) { return; }
      if (elH > neighborH) { h = elH; }
      else { h = neighborH; }
      el.height(h + 'px');
      neighbor.height(h + 'px');
    });
  }
  function resetAndResizeArticles() {
    resetArticles();
    if (win.width() > 850) {
      resizeArticles();
    }
  }
  function resizeContent() {
    var diff = win.height() - $('#wrapper').height()
      , content = $('#content')
      , contentH = content.height();
    if (diff > 0) {
      content.height((contentH + diff) + 'px');
    }
  }
  function resetAndResizeContent() {
    $('#content').attr('style', '');
    resizeContent();
  }
})();

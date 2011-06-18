(function() {
  var win = $(window)
    , t = false;

  $.script.path = '/js/';
  $.domReady(resetAndResizeArticles);
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

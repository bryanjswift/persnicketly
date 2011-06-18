(function() {
  $.script.path = '/js/';
  $.domReady(evenArticles);
  var win = $(window)
    , t = false;
  function evenArticles() {
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
  function resetArticles() {
    $('.list li').attr('style', '');
  }
  $(window).addListener('resize', function resizeTest() {
    if (t) { clearTimeout(t); }
    t = setTimeout(function resizeTimeout() {
      resetArticles();
      if (win.width() > 850) {
        setTimeout(evenArticles, 1);
      }
      t = false;
    }, 100);
  });
})();

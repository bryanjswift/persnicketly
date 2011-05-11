(function(doc, tag, win) {
  var s = doc.createElement(tag)
    , el = doc.getElementsByTagName(tag)[0]
    , nav = win.navigator
    , l = location
    , params = {
        "userAgent": nav.userAgent
      , "language": nav.language
      , "referrer": (document.referrer || document.referer)
      };
  win.mpq = [
    ["init","b13487272824c77655227e84f830d7ee"],
    ["register", params],
    ["track", "pageview", { "path": l.path || l.pathname, "domain": l.host || l.hostname, "q": l.search }]
  ];
  s.src = "//api.mixpanel.com/site_media/js/api/mixpanel.js";
  s.async = 1;
  if (location.href.match(/.*persnicketly\.com.*/)) {
    el.parentNode.insertBefore(s, el);
  }
})(document, "script", window);

var mpq = (function(doc, tag, win) {
  var s = doc.createElement(tag)
    , el = doc.getElementsByTagName(tag)[0]
    , nav = win.navigator
    , params = {
        "url": doc.location.href
      , "userAgent": nav.userAgent
      , "language": nav.language
      , "referrer": (document.referrer || document.referer)
      };
  s.src = "//api.mixpanel.com/site_media/js/api/mixpanel.js";
  s.async = 1;
  el.parentNode.insertBefore(s, el);
  return [
    ["init","b13487272824c77655227e84f830d7ee"],
    ["register", params ],
    ["track", "pageview"]
  ];
})(document, "script", window);

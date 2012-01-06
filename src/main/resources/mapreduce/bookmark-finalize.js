function (k, v) {
  v.score = v.favorite_count + v.count;
  return v;
}

# Welcome to Persnicketly #

The goal of this webservice is to help users of [Readability][1] find high quality content to read. Persnicketly's approach to this problem at a high level is simple. We look at the bookmark feeds of any Readability user that allows us to and extract their starred or favorited articles. By aggregating this information we can help users be persnickety[2] about what they spend their most precious resource (focus/attention) on.

## How? ##

In order for Persnicketly to be able to read data from Readability's API people will have to approve Persnicketly as an oAuth consumer. This means we can only read data people opt-in and allow us to read. When a user authenticates with oAuth we store the various oAuth tokens and the verifier, we use this information to periodically poll Readability for newly added or updated bookmarks. Bookmarks are pulled into Persnicketly's database and then periodically scored to determine their display order in top bookmarks. Eventually each bookmark will also be categorized (see below) and sorted within each category as well.

## There's more to it, right? ##

In addition to bubbling good content to the top of the persnicketly reading list we would also like to make it easier to browse good content you're interested in by putting the good stuff in big buckets; buckets with labels like:

* Education
* Fiction
* Health
* Politics
* Technology



[1]: http://readability.com
[2]: http://dictionary.reference.com/browse/persnickety 

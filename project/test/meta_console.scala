import com.persnicketly.web.Persnicketly
import com.persnicketly.persistence.UserDao
import com.persnicketly.readability.Api
val dao = new UserDao
val user = dao.get("kPUWXBhKSktnCsNW6p").get
val meta = Api.bookmarksMeta(Persnicketly.oauthConsumer, user)


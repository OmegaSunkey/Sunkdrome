package omega.sunkey.sunkdrome.server.dataclasses

import com.fasterxml.jackson.annotation.JsonInclude

data class Indexes(
    val ignoredArticles: List<String>,
    val index: List<Index>
)

data class Index(
    val name: String,
    @param:JsonInclude(JsonInclude.Include.NON_EMPTY)
    val artist: List<ArtistData>,
    @param:JsonInclude(JsonInclude.Include.NON_EMPTY)
    val child: List<AlbumData>
)

data class IndexesView(
    val indexes: Indexes
)
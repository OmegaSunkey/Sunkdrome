package omega.sunkey.sunkdrome.server.dataclasses

import com.fasterxml.jackson.annotation.JsonInclude

data class Indexes(
    val ignoredArticles: List<String>,
    val index: List<Index>
)

data class Index(
    val name: String,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val artist: List<ArtistData>,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val child: List<AlbumsWithoutSongs>
)

data class IndexesView(
    val indexes: Indexes
)
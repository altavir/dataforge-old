package hep.dataforge.site

import com.sysgears.grain.taglib.Site
import org.yaml.snakeyaml.Yaml

/**
 * Change pages urls and extend models.
 */
class ResourceMapper {

    /**
     * Site reference, provides access to site configuration.
     */
    private final Site site

    private static final dateFormat = "dd.MM.yyyy";

    public ResourceMapper(Site site) {
        this.site = site
    }

    /**
     * This closure is used to transform page URLs and page data models.
     */
    def map = { resources ->

        List refinedResources = resources.findResults(filterPublished).collect { Map resource ->
            fillDates << resource
        }


        injectNews(refinedResources)
        injectShards(refinedResources)
        injectReleases(refinedResources)
        loadSections(refinedResources)

        refinedResources
    }

    /**
     * Inject releases
     */
    private def injectReleases(List resources) {
        //Adding news shards
        Map newsPage = resources.find { it.url == '/releases.html' };
        def releases = resources.findAll { it.content_type == 'release' }
        releases.each {
            it << ["notes_ref"  : it.notes_ref ?: "/docs/${it.version_name}/notes.txt",
                   "javadoc_ref": it.javadoc_ref ?: "/docs/${it.version_name}/javadoc/index.html"]
        }
        //TODO add sorting here
        newsPage.put("releases", releases)

        //removing releases from output
        resources.removeAll(releases)
    }

    /**
     * Inject news shards
     */
    private def injectNews(List resources) {
        //Adding news shards
        Map newsPage = resources.find { it.url == '/news.html' };
        def news = resources.findAll { it.url =~ /news\/.*/ && it.content_type == 'news_shard' }
        news.sort(new OrderBy({ it.date }))
        newsPage.put("news", news)
        //removing news from published pages
        resources.removeAll(news)
    }


    private def loadSections(List resources){
        def header = readConfig('/config/header.yml', resources)
        //println "\n Header configuration: ${header} \n"
        resources.findAll{it.layout}.each{
            Map page = it
            page.putAll(header)
        }
    }

    /**
     * Inject doc shards in resource map
     */
    private def injectShards(List resources) {
        Map docsPage = resources.find { it.url == '/docs.html' };
        def shards = resources.findAll { (it.url =~ /shards\/.*/ && it.content_type == 'doc_shard') }
        shards.sort { shard1, shard2 ->
            for (int i = 0; i < shard1.ordering.size(); i++) {
                def index1 = shard1.ordering[i];
                def index2 = shard2.ordering[i];
                if (!index2 || index1 > index2) {
                    return 1
                } else if(index2 > index1){
                    return -1
                }
            }
            return -1;
        }
        List curIndexes = new ArrayList();
        List lastOrdering = new ArrayList();

        shards.forEach {
            for (int i = 0; i < it.ordering.size(); i++) {
                if (!curIndexes[i]) {
                    curIndexes[i] = 1;
                } else if (it.ordering[i] != lastOrdering[i]) {
                    curIndexes[i] = curIndexes[i] + 1;
                    //zeroing following indexes on chapter change
                    for (int j = i + 1; j < curIndexes.size(); j++) {
                        curIndexes[j] = 0;
                    }
                }
            }

            lastOrdering = it.ordering;
            it << ["section": curIndexes.findAll { it }.join(".")]
        }


        docsPage.put("shards", shards)

        //remove shards from output
        resources.removeAll(shards)
    }


    private def findResource(String name, List resources){
        resources.find{it.url == name}
    }

    private def readConfig(String name, List resources){
        File configFile = new File(site.content_dir as String,name)
        Yaml yaml = new Yaml();
        yaml.load(configFile.text) as Map ?: [:]
    }

    /**
     * Excludes resources with published property set to false,
     * unless it is allowed to show unpublished resources in SiteConfig.
     */
    private def filterPublished = { Map it ->
        (it.published != false || site.show_unpublished) ? it : null
    }

    /**
     * Fills in page `date` and `updated` fields
     */
    private def fillDates = { Map it ->
        def update = [date   : it.date ? Date.parse(dateFormat, it.date) : new Date(it.dateCreated as Long),
                      updated: it.updated ? Date.parse(dateFormat, it.updated) : new Date(it.lastUpdated as Long)]

        it + update
    }
}

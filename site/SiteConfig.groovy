import hep.dataforge.site.ResourceMapper
import hep.dataforge.site.ThemeTagLib

// Resource mapper and tag libs.
resource_mapper = new ResourceMapper(site).map
tag_libs = [ThemeTagLib]
datetime_format = "dd.MM.yyyy"

non_script_files = [/(?i).*\.(js|css)$/]

excludes << '.*\\.yml'
excludes << '.*\\.rb'
binary_files << '.*\\.pdf'

features {
    highlight = 'pygments' // 'none', 'pygments'
    markdown = 'txtmark'   // 'txtmark', 'pegdown'
    asciidoc {
        opts = ['source-highlighter': 'coderay',
                'icons': 'font']
    }
}

environments {
    dev {
        log.info 'Development environment is used'
        url = "http://localhost:${jetty_port}"
        show_unpublished = true
    }
    prod {
        log.info 'Production environment is used'
        generate_absolute_links = true
        url = 'http://npm.mipt.ru/dataforge' // site URL, for example http://www.example.com
        show_unpublished = false
        generate_absolute_links = true
        features {
            minify_xml = false
            minify_html = false
            minify_js = false
            minify_css = false
        }
    }
    cmd {
        features {
            compass = 'none'
            highlight = 'none'
        }
    }
}

python {
    interpreter = 'jython' // 'auto', 'python', 'jython'
    //cmd_candidates = ['python2', 'python', 'python2.7']
    //setup_tools = '2.1'
}

ruby {
    interpreter = 'jruby'   // 'auto', 'ruby', 'jruby'
    //cmd_candidates = ['ruby', 'ruby1.8.7', 'ruby1.9.3', 'user.home/.rvm/bin/ruby']
    //ruby_gems = '2.2.2'
}

<nav class="navbar navbar-default">
    <div class="container">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar"
                    aria-expanded="false" aria-controls="navbar">
                <span class="sr-only">Toggle navigation</span>
            <#--<span class="icon-bar"></span>-->
            <#--<span class="icon-bar"></span>-->
            <#--<span class="icon-bar"></span>-->
            </button>
            <a href="${homeURL}">
                <img style="display: block; float: left; padding: 5px 10px 0 0px" alt="DataForge" height="45"
                     src="images/df_logo.png"/>
            </a>
            <!--<a class="navbar-brand" href="http://npm.mipt.ru/dataforge/">DataForge</a>-->
        </div>
        <div id="navbar" class="navbar-collapse collapse">
            <ul class="nav navbar-nav">
            <#list navigation?sort as nav>
                <li><a href="${homeURL}/${nav}">${nav}</a></li>
            </#list>
            </ul>
        </div>
        <!--/.nav-collapse -->
    </div>
</nav>
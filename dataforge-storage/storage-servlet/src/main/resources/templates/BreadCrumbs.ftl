<div class="container">
    <ol class="breadcrumb">
        <li><a href="${homeURL}">Root</a></li>
        <#list path as item>
            <li>${item}</li>
        </#list>
        <#if loaderName??>
            <li>${loaderName}</li>
        </#if>
    </ol>
</div>

<script type="text/javascript">
    var jq = jQuery;

    jq(document).ready(function(){
        jq("#${config.formFieldName}").val("${disabledLocations}"+"${userLocationProperty}");
        jq(".location${config.formFieldName}").change(function () {
            var locations_val = [];
            jq.each(jq(".location${config.formFieldName}"), function(){
                if(jq(this).is(':checked')){
                    locations_val.push(jq(this).val());
                }
            });
            jq("#${config.formFieldName}").val("${disabledLocations}"+locations_val.join(","));
        });
    });
</script>

<label id= ${config.formFieldName} >
    ${ui.message("locationbasedaccess.location")}
</label>
<table class="adminui-display-table" cellspacing="0" cellpadding="0">
<% activeLocations.eachWithIndex{ location, index -> %>
${index % 2 == 0 ? '<tr>' : ''}
<td valign="top">
    <div><input id="adminui-loc-${location.uuid}" class = "location${config.formFieldName}" type="checkbox" name="${config.formFieldName}"
                value="${location.uuid}"
        <% if (selectedLocationUuids.contains(location.uuid)) { %> checked="checked"<% } %>/>
        <label for="adminui-loc-${location.uuid}">${location.name}</label>
    </div>
</td>
<% if(index % 2 != 0 || index == (activeLocations.size - 1)) { %>
${index % 2 == 0 ? '<td valign="top"></td>' : ''}
</tr>
<% } %>
<% } %>
</table>

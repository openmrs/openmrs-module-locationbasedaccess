
<div>
    <div>
        <p>
            <label>
                ${ui.message("locationbasedaccess.location")}
            </label>
            <select id="${config.formFieldName}" name="${config.formFieldName}">
                <% activeLocations.each { location -> %>
                <option value="${location.uuid}"
                    ${ ui.encodeHtmlContent(location.uuid==selectedLocationUuid ? ' selected': '') }
                >${location.name}</option>
                <% } %>
            </select>
        </p>
    </div>
</div>


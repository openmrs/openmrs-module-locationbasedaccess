
<div>
    <div>
        <p class="left">
            <label>
                Please select the location
            </label>
            <select id="${config.formFieldName}" name="${config.formFieldName}">
                <% activeLocations.each { location -> %>
                <option value="${location.uuid}">${location.name}</option>
                <% } %>
            </select>
        </p>
    </div>
</div>


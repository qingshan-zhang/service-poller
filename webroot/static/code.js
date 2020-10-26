// TODO: find out rootcause of script change is not picked up by Gradle
const listContainer = document.querySelector('#service-list');
if (getCookie("kry") === "") {
    let cookieValue = Math.random().toString(36).substring(7);
    document.cookie = "kry= " + cookieValue + "; expires=Thu, 18 Dec 2020 12:00:00 UTC; path=/";
}
let servicesRequest = new Request('/service');
fetch(servicesRequest, {
    method: 'get',
    headers: {
        'Cookie': getCookie("kry")
    }
})
    .then(function(response) {return response.json();})
    .then(function(serviceList) {
        serviceList.forEach(service => {
            let li = document.createElement("li");
            let button = document.createElement("button");
            button.innerText = "delete";
            button.addEventListener("click", () => {
                listContainer.removeChild(li);
                sendDelete(service.name)
            });
            li.appendChild(document.createTextNode(service.name + ': ' + service.status));
            li.appendChild(button);
            listContainer.appendChild(li);
        });
    });

const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt => {
    let urlName = document.querySelector('#url-name').value;
    fetch('/service', {
        method: 'post',
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({url:urlName})
    }).then(res=> {
        //TODO: handle 400 bad request and alert bad url format
        location.reload();
    });
}

function sendDelete(sname) {
    fetch('/service', {
        method: 'delete',
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json',
            'Cookie': getCookie("kry")
        },
        body: JSON.stringify({url:sname})
    }).then(res => {
        location.reload();
    })
}

function getCookie(cname) {
    let name = cname + "=";
    let ca = document.cookie.split(';');
    for(let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!-- Standard Meta -->
<meta charset="utf-8"/>
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
<script src="http://code.jquery.com/jquery-1.9.1.js"></script>
<link rel="stylesheet" type="text/css" href="./resource/semantic.min.css">
<script src="./resource/semantic.min.js"></script>

<style type="text/css">
    body {
        background-color: #FFFFFF;
    }

    .ui.menu .item img.logo {
        margin-right: 1.5em;
    }

    .main.container {
        margin-top: 7em;
    }

    .wireframe {
        margin-top: 2em;
    }

    .ui.footer.segment {
        margin: 5em 0em 0em;
        padding: 5em 0em;
    }
</style>
<div class="ui fixed inverted menu">
    <div class="ui container">
        <div href="#" class="header item">
            <img class="logo" src="resource/image/logo.png">
            LegendZone
        </div>
        <a href="/" class="item">Home</a>
        <a href="#" class="ui simple dropdown item">
            Dropdown <i class="dropdown icon"></i>
            <div class="menu">
                <div class="item">Link Item</div>
                <div class="item">Link Item</div>
                <div class="divider"></div>
                <div class="header">Header Item</div>
                <div class="item">
                    <i class="dropdown icon"></i>
                    Sub Menu
                    <div class="menu">
                        <div class="item">Link Item</div>
                        <div class="item">Link Item</div>
                    </div>
                </div>
                <div class="item">Link Item</div>
            </div>
        </a>
    </div>
</div>

<div class="ui small test modal transition hidden" style="margin-top: -92.5px;">
    <div class="header">Notice</div>
    <div class="content">
        <p>Content</p>
    </div>
    <div class="actions">
        <!--<div class="ui negative button">No </div>-->
        <div class="ui positive right labeled icon button">OK <i class="checkmark icon"></i> </div>
    </div>
</div>

<script>
    var showModal;
    showModal = function (text) {
        $(".ui.modal").children(".content").html(text);
        $('.ui.modal').modal('show');
    }
</script>
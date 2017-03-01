<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>LegendZone</title>

    <!-- Standard Meta -->
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">

    <!-- Site Properities -->

    <link rel="stylesheet" type="text/css" href="./resource/semantic.css">


    <script src="http://ajax.aspnetcdn.com/ajax/jQuery/jquery-1.8.0.js"></script>

    <script src="./resource/semantic.js"></script>

    <script>
        $(document)
                .ready(function() {
                    $('.ui.selection.dropdown').dropdown();
                    $('.ui.menu .ui.dropdown').dropdown({
                        on: 'hover'
                    });
                })
        ;
    </script>


    <link rel="stylesheet" type="text/css" href="./resource/components/reset.css">
    <link rel="stylesheet" type="text/css" href="./resource/components/site.css">

    <link rel="stylesheet" type="text/css" href="./resource/components/container.css">
    <link rel="stylesheet" type="text/css" href="./resource/components/grid.css">
    <link rel="stylesheet" type="text/css" href="./resource/components/header.css">
    <link rel="stylesheet" type="text/css" href="./resource/components/image.css">
    <link rel="stylesheet" type="text/css" href="./resource/components/menu.css">

    <link rel="stylesheet" type="text/css" href="./resource/components/divider.css">
    <link rel="stylesheet" type="text/css" href="./resource/components/list.css">
    <link rel="stylesheet" type="text/css" href="./resource/components/segment.css">
    <link rel="stylesheet" type="text/css" href="./resource/components/dropdown.css">
    <link rel="stylesheet" type="text/css" href="./resource/components/icon.css">

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

</head>
<body>



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


<div class="ui inverted vertical footer segment">
    <div class="ui center aligned container">
        <div class="ui stackable inverted divided grid">
            <div class="three wide column">
                <h4 class="ui inverted header">Group 1</h4>
                <div class="ui inverted link list">
                    <a href="#" class="item">Link One</a>
                    <a href="#" class="item">Link Two</a>
                    <a href="#" class="item">Link Three</a>
                    <a href="#" class="item">Link Four</a>
                </div>
            </div>
            <div class="three wide column">
                <h4 class="ui inverted header">Group 2</h4>
                <div class="ui inverted link list">
                    <a href="#" class="item">Link One</a>
                    <a href="#" class="item">Link Two</a>
                    <a href="#" class="item">Link Three</a>
                    <a href="#" class="item">Link Four</a>
                </div>
            </div>
            <div class="three wide column">
                <h4 class="ui inverted header">Group 3</h4>
                <div class="ui inverted link list">
                    <a href="#" class="item">Link One</a>
                    <a href="#" class="item">Link Two</a>
                    <a href="#" class="item">Link Three</a>
                    <a href="#" class="item">Link Four</a>
                </div>
            </div>
            <div class="seven wide column">
                <h4 class="ui inverted header">Footer Header</h4>
                <p>Extra space for a call to action inside the footer that could help re-engage users.</p>
            </div>
        </div>
        <div class="ui inverted section divider"></div>
        <img src="resource/image/logo.png" class="ui centered mini image">
        <div class="ui horizontal inverted small divided link list">
            <a class="item" href="#">Site Map</a>
            <a class="item" href="#">Contact Us</a>
            <a class="item" href="#">Terms and Conditions</a>
            <a class="item" href="#">Privacy Policy</a>
        </div>
    </div>
</div>



</body>
</html>


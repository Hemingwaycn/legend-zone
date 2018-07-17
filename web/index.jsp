<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
    <head>
        <link rel="shortcut icon" href="resource/image/logo.png">
        <title>LegendZone</title>
    </head>
    <body>

    <%@ include file="/header.jsp"%>
        <div class="ui main text container">
            <div class="row">
                <div class="column">
                    <h1 class="ui header">Welcome to Legend-Zone!</h1>
                    <div class="ui divider"></div>
                    <div>    感谢传说大魔王赞助服务器. <br/>更新:魔方计时器<br/> <br/> </div>
                </div>
            </div>

            <div class="row">
                <div class="column">
                    <h1 class="ui header">Games</h1>
                    <div class="ui divider"></div>
                    <a class="ui primary button" tabindex="0" href="<c:url value="/blackAndWhite" />">
                        Black And White
                    </a>
                    <a class="ui primary button" tabindex="0" href="<c:url value="/scriptsAndScribes" />">
                        Scripts And Scribes
                    </a>
                    <a class="ui primary button" tabindex="0" href="<c:url value="/gomoku" />">
                        Gomoku
                    </a>
                    <br/> <br/>
                </div>
            </div>

            <div class="row">
                <div class="column">
                    <h1 class="ui header">Timer</h1>
                    <div class="ui divider"></div>
                    <a class="ui primary button" tabindex="0" href="/timer">
                        Cube Timer
                    </a>
                </div>
            </div>

        </div>
    <%@ include file="/footer.jsp"%>
    </body>

</html>








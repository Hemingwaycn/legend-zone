<%--@elvariable id="pendingGames" type="java.util.Map<long, java.Lang.String>"--%>
<!DOCTYPE html>
<html>
    <head>
        <link rel="shortcut icon" href="resource/image/logo.png">
        <title>LegendZone :: Scripts And Scribes</title>
        <script src="http://code.jquery.com/jquery-1.9.1.js"></script>
    </head>
    <body>
    <%@ include file="/header.jsp"%>
        <div class="ui main text container">
            <h2>Scripts And Scribes</h2>
            <a href="javascript:void 0;" onclick="startGame();">Start a Game</a><br />
            <br />
            <a href="javascript:void 0;" onclick="vsAi();">Play against AI</a><br />
            <br />
            <c:choose>
                <c:when test="${fn:length(pendingGames) == 0}">
                    <i>There are no existing games to join.</i>
                </c:when>
                <c:otherwise>
                    Join a game waiting for a second player:<br />
                    <c:forEach items="${pendingGames}" var="e">
                        <a href="javascript:void 0;"
                           onclick="joinGame(${e.key});">User: ${e.value}</a><br />
                    </c:forEach>
                </c:otherwise>
            </c:choose>

        </div>
        <script type="text/javascript" language="javascript">
            var startGame, joinGame, vsAi;
            $(document).ready(function() {
                var url = '<c:url value="/scriptsAndScribes" />';

                startGame = function() {
                    var username = prompt('Enter a username to start a game.', '');
                    if(username != null && username.trim().length > 0 &&
                            validateUsername(username))
                        post({action: 'start', username: username});
                };

                vsAi = function() {
                    var username = prompt('Enter a username to start a game.', '');
                    if(username != null && username.trim().length > 0 &&
                            validateUsername(username))
                        post({action: 'vsAI', username: username});
                };

                joinGame = function(gameId) {
                    var username =
                            prompt('Enter a username to join this game.', '');
                    if(username != null && username.trim().length > 0 &&
                            validateUsername(username))
                        post({action: 'join', username: username, gameId: gameId});
                };

                var validateUsername = function(username) {
                    var valid = username.match(/^[a-zA-Z0-9_]+$/) != null;
                    if(!valid)
                        alert('User names can only contain letters, numbers ' +
                                'and underscores.');
                    return valid;
                };

                var post = function(fields) {
                    var form = $('<form id="mapForm" method="post"></form>')
                            .attr({ action: url, style: 'display: none;' });
                    for(var key in fields) {
                        if(fields.hasOwnProperty(key))
                            form.append($('<input type="hidden">').attr({
                                name: key, value: fields[key]
                            }));
                    }
                    $('body').append(form);
                    form.submit();
                };
            });
        </script>
        <%@ include file="/footer.jsp"%>
    </body>
</html>

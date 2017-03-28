package org.dmw.mafia;

import org.apache.commons.lang3.math.NumberUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(
        name = "mafiaServlet",
        urlPatterns = "/mafia"
)
public class MafiaServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setAttribute("pendingGames", MafiaGame.getPendingGames());
        this.view("list", request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String action = request.getParameter("action");
        if("join".equalsIgnoreCase(action))
        {
            String gameIdString = request.getParameter("gameId");
            String username = request.getParameter("username");
            if(username == null || gameIdString == null ||
                    !NumberUtils.isDigits(gameIdString))
                this.list(request, response);
            else
            {
                request.setAttribute("action", "join");
                request.setAttribute("username", username);
                request.setAttribute("gameId", Long.parseLong(gameIdString));
                this.view("game", request, response);
            }
        }
        else if("create".equalsIgnoreCase(action))
        {
            String username = request.getParameter("username");
            if(username == null)
                this.list(request, response);
            else
            {
                request.setAttribute("action", "create");
                request.setAttribute("username", username);
                request.setAttribute("gameId", MafiaGame.queueGame(username));
                this.view("game", request, response);
            }
        }else{
            this.list(request, response);
        }

    }

    private void view(String view, HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException
    {
        request.getRequestDispatcher("/WEB-INF/jsp/view/mafia/"+view+".jsp")
               .forward(request, response);
    }

    private void list(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        response.sendRedirect(response.encodeRedirectURL(
                request.getContextPath() + "/mafia"
        ));
    }
}

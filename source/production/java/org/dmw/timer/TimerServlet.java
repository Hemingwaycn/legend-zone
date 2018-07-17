package org.dmw.timer;

import org.apache.commons.lang3.math.NumberUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(
        name = "timerServlet",
        urlPatterns = "/timer"
)
public class TimerServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setAttribute("pendingGames", TimerGame.getPendingGames());
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
        else if("start".equalsIgnoreCase(action))
        {
            String username = request.getParameter("username");
            if(username == null)
                this.list(request, response);
            else
            {
                request.setAttribute("action", "start");
                request.setAttribute("username", username);
                request.setAttribute("gameId", TimerGame.queueGame(username));
                this.view("game", request, response);
            }
        }else if("vsAI".equalsIgnoreCase(action))
        {
            String username = request.getParameter("username");
            if(username == null)
                this.list(request, response);
            else
            {
                request.setAttribute("action", "vsAI");
                request.setAttribute("username", username);
                request.setAttribute("gameId", TimerGame.queueGame(username));
                this.view("game", request, response);
            }
        }
        else
            this.list(request, response);
    }

    private void view(String view, HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException
    {
        request.getRequestDispatcher("/WEB-INF/jsp/view/timer/"+view+".jsp")
               .forward(request, response);
    }

    private void list(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        response.sendRedirect(response.encodeRedirectURL(
                request.getContextPath() + "/timer"
        ));
    }
}

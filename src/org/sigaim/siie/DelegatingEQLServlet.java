package org.sigaim.siie;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openehr.am.parser.ContentObject;
import org.sigaim.siie.clients.IntSIIE001EQLClient;
import org.sigaim.siie.clients.ws.WSIntSIIE001EQLClient;
import org.sigaim.siie.dadl.DADLManager;
import org.sigaim.siie.dadl.OpenEHRDADLManager;
import org.sigaim.siie.seql.model.SEQLResultSet;

/**
 * Servlet implementation class DelegatingEQLServlet
 */
@WebServlet("/index.html")
public class DelegatingEQLServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private IntSIIE001EQLClient eqlClient;
	private DADLManager dadlManager;

    /**
     * Default constructor. 
     */
    public DelegatingEQLServlet() {
    	eqlClient=new WSIntSIIE001EQLClient("http://sigaim.siie.cesga.es:8080/SIIEWS3/services/INTSIIE001EQLImplService/");
    	dadlManager=new OpenEHRDADLManager();
    }
    public static String encodeHTML(String s)
    {
        StringBuffer out = new StringBuffer();
        for(int i=0; i<s.length(); i++)
        {
            char c = s.charAt(i);
            if(c > 127 || c=='"' || c=='<' || c=='>')
            {
               out.append("&#"+(int)c+";");
            }
            else
            {
                out.append(c);
            }
        }
        return out.toString();
    }
    protected void printOutput(PrintWriter out, String query, SEQLResultSet result) throws Exception{
    	out.println("<html>");
    	out.println("<head>");
    	out.println("<title>");
    	out.println("Interfaz Web EQL");
    	out.println("</title>");
    	out.println("</head>");
    	out.println("<body>");
    	out.println("<h1 style=\"width: 100%; text-align: center\">Interfaz Web EQL</h1>");
    	out.println("<p style=\"text-align: center\"><img src=\"Images/logo_header.png\"</p>");
    	out.println("<p style=\"text-align: center\">Introduce una consulta y pulsa Enviar</p>");
    	out.println("<div style=\"width: 100%; text-align: center\">");
    	out.println("<form name=\"form\" method=\"post\" action=\"\">");
    	out.println("<textarea name=\"query\" cols=\"100\" rows=\"10\">");
    	out.println(query);
    	out.println("</textarea>");
    	out.println("<br/><input type=\"submit\" value=\"Enviar\">");
    	if(result==null) {
    		out.println("<p>Se ha producido un error en la consulta. Por favor, revisa la sintaxis</p>");
    	} else {
    		out.println("<p>Resultados de la consulta: </p>");
    		out.println("<div style=\"width: 100%; text-align: left\"><table>");
			int flag=0;
			int width=(int) ((float)100/(float)result.getNumberOfColumns());
    		while(result.nextRow()) {
    			out.println("<tr>");
    			int i;
    			for(i=0;i<result.getNumberOfColumns();i++) {
    				ContentObject obj=result.getColumn(i);
    				if(flag>0) {
    					out.println("<td style=\"font-size: 50%;\" width=\""+width+"%\"><code>");
    				} else{
    					out.println("<td style=\"font-size: 50%;  background-color: #EEEEEE;\" width=\""+width+"%\"><code>");
    				}
    				String serialized=this.dadlManager.serialize(obj,false);
    			    out.println(encodeHTML(serialized));
    				out.println("</code></td>");
    			}
   				flag++;
				flag%=2;
    			out.println("</tr>");
    		}
    		out.println("</table></div>");
    	}
    	out.println("</form></div>");
    	out.println("</body>");
    	out.println("</html>");
    }
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out=response.getWriter();
		String query=request.getParameter("query");
		if(query==null) {
			query="SELECT e FROM EHR e;";
		}
		SEQLResultSet result=null; 
		try {
			result=eqlClient.query("", query);
		} catch(Exception e) {
			e.printStackTrace();
			result=null;
		}
		try {
			printOutput(out,query,result);
		} catch(Exception e) {
			throw new IOException("Error interno inesperado");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}

}

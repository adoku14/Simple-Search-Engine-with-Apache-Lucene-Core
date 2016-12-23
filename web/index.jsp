<%-- 
    Document   : index
    Created on : Dec 4, 2016, 9:59:03 AM
    Author     : user
--%>

<%@page import="java.util.Date"%>
<%@page import="org.apache.lucene.search.suggest.Lookup"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="logic.Searcher" %>
<%@page import="logic.Dokument" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Search Engine</title> 
        <script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
        <link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
        
        <style>
    
             .form-control {
                position: relative;
                height: auto;
                left: 2px;
                margin-bottom: 20px;
                width: 400px;
                border-radius: 8px;
                -webkit-box-sizing: border-box;
                   -moz-box-sizing: border-box;
                        box-sizing: border-box;
                padding: 10px;
                font-size: 16px;
              }
              .form-control:focus {
                z-index: 2;
              }
              #search{
                position: absolute;
                right:1%;
                top: 5%;
                width:370px;
                height: 40px;
              }
              #button {
                position: absolute;
                 right:25%;
                top: 5%;
                width:50px;
                height: 40px;
                }
                .div{
                   position: absolute;
                    right:5%;
                   top: 13%;
                   width:525px;
                   height: 50px;
                }
               
                .left{
                     border-right: 1px solid grey;
                    position:absolute;
                    left:2%;
                    top:12%;
                    height:100%;
                    width:62%;
                    
                }
                .right{
                    
                    position:absolute;
                    right:0%;
                    height:100%;
                    width:35%;
                    top:10%;
                }
                .header{
                    min-height: 20px;
                    border-bottom: 1px solid grey;
                     
                }
        
        </style>
        
        <%
           
            String x = request.getParameter("submit");
            Dokument dok = null;
             String sug = null;
             String query = null;
             String suggestions = "";
             List<Lookup.LookupResult> list = null;
           
            Double second = 0.0;
              Searcher result = new Searcher();
            List<Dokument> doclist = new ArrayList<Dokument>();
            
                 int start = 10;
                     if(x !=null)
            {
                Date begin = new Date();
                 query = request.getParameter("search");
               
                String check2 = request.getParameter("title");
                String selected = request.getParameter("analyzer");
                
                
                    
                    if(check2 != null)
                    {
                       
                          doclist = result.SearchTitle(query,selected);
                    }else
                    {
                       
                        doclist = result.Search(query, selected);
                    }
                
                 list = result.results;
     /*
                if(query != null)
                     if(list.size() != 0)
                     {
                        for(Lookup.LookupResult res : list)
                        {
                            suggestions += res.key + " ";
                        }
                     }
*/
                int num = 0;
                if(query != null)
                {
                    ArrayList<String> sugg = result.data;
                    if(sugg.size() != 0)
                    num = Integer.parseInt(sugg.get(0));
                    if(sugg.size() > 1)
                        for(int i = 1; i < sugg.size(); i++)
                        {

                            suggestions += sugg.get(i) + " ";
                        }
                              
                }
               
                if(doclist.size() == 0)
                {
                    out.println("No Documents are Found");
                   
                    
                 String [] words = query.split(" ");
                   query = "";
                 for (String w: words){
                        query += w + "* ";
                 }
                   
                              
                       request.setAttribute(query, "search");
                    if(check2 != null)
                    {
                       
                          doclist = result.SearchTitle(query,selected);
                    }else
                    {
                        doclist = result.Search(query, selected);
                        
                    }   
                }     
               Date end = new Date();
               second = (double)(end.getTime() - begin.getTime())/1000;
            }
                 

                
            %>
            
           
    </head>
    <body>
        <h2 align="center" class='header'>Welcome to Information Retrieval Project</h2>
        
        <div class="right">
        <fieldset >
            <form  method="GET" action="index.jsp">
                <input type="text" placeholder="Enter your text to Search for" id="search" name="search" class="form-control" value= '<%=query %>'/>
                 <button class="glyphicon glyphicon-search" id="button" name="submit" type="submit"></button> 
                 <div class='div'>
                 
                 <select name='analyzer'>
                    <option value="StandardAnalyzer">StandardAnalyzer</option>
                    <option value="StopAnalyzer">StopAnalyzer</option>
                    <option value="WhiteSpaceAnalyzer">WhiteSpaceAnalyzer</option>
                    <option value="SimpleAnalyzer">SimpleAnalyzer</option>
                  </select>
                 &nbsp; &nbsp; &nbsp;
                 <input type="checkbox" name="title" value='Title'> Title &nbsp; &nbsp;
                 
                 <a href="http://localhost:8080/Search_Engine/index.jsp?search=<% 
                     String test = "";
                     if(query != null)
                     {
                         String[] words = suggestions.split(" ");
                        for(String word : words)
                        {
                            test += word + "+";
                        }
                     }
                     out.write(test);
                    %>&submit=&analyzer=StandardAnalyzer">Suggestion: <%=suggestions %></a>
                 </div>
            </form>
             </fieldset>
        </div>
            <div class="left">
              <%
                
                  if(second != 0.0)
                  out.println("<p>Time to search    "+ second + "</p>");
                  out.println("<p> Total Found  " + result.totalnumberhits + " documents </p>");
                int e = Math.min(20, doclist.size());
                for(int i = 0; i < e;i++)
                    {
                        dok = doclist.get(i);
                
                 out.write("<p>");
                 String path = dok.getPath();
                 %>
                 <a href=<%="file:///"+path%> />
                 
                     <%
                   out.write(dok.getId() + ":  " + dok.getTitle() ); out.write("</a>");
                                    out.println("<br/> this Document has score of: " + dok.getScore());
                     out.write(" </p><br/>");}
                
            
                    %>
                                 
            </div>
            
            
       
   
    </body>
</html>

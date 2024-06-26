package com.sgdbf.rest.api;

import groovy.json.JsonBuilder
import groovy.sql.Sql

import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

import javax.naming.Context
import javax.naming.InitialContext
import javax.naming.NamingException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.sql.DataSource

import org.apache.http.HttpHeaders
import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController

class Index implements RestApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(Index.class)

    @Override
    RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
        
        def name = request.getParameter "name"
        if (name == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"code" : "99", "status" : "KO", "error" : "the parameter name is missing"}""")
        }
		
		Connection con = null
		def usrList =[]
		def result = null
		Statement requestStatement = null
		ResultSet rs = null
		
		try
		{
			Properties props = loadProperties("configuration.properties", context.resourceProvider)
			
			String dataSource = props["db_datasource"]
			
			Context envCtx = (Context) new InitialContext()
			DataSource ds = (DataSource) envCtx.lookup(dataSource);
			con = ds.getConnection()
			
			requestStatement = con.createStatement()
			
			String qry = "select * from user_ where (lower(username) like lower('"+name+"%') or lower(firstname) like lower('"+name+"%') or lower(lastname) like lower('"+name+"%')) and tenantid = 1  and enabled = true LIMIT 30"
			
			rs = requestStatement.executeQuery(qry)
			
			while (rs.next())
			{
				Map usrInfo = new HashMap()
				
				usrInfo.put("userId", rs.getInt("id"))
				usrInfo.put("userName", rs.getString("username"))
				usrInfo.put("firstName", rs.getString("firstname"))
				usrInfo.put("lastName", rs.getString("lastname"))
				
				usrList.add(usrInfo)
			}
			
			
			
			if(usrList.size() > 0)
			{
				result = [ "code" : "00" , "status" : "OK" , "data" : usrList]
			}
			else {
				result = [ "code" : "02" , "status" : "KO" , "description" : "No data found" , "data" : usrList ]
			}
			
		   
		} catch (Exception e) {
			
			def err = [ "code" : "99", "status" : "K0", "error" : e.getMessage()]
			return buildResponse(responseBuilder, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,new JsonBuilder(err).toPrettyString())
		} finally {
			try {
				rs.close()
				requestStatement.close()
				con.close()
			} catch (Exception e) {
			
				def err = [ "code" : "99", "status" : "K0", "error" : e.getMessage()]
				return buildResponse(responseBuilder, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,new JsonBuilder(err).toPrettyString())
			} 
		}

        return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder(result).toString())
    }

    /**
     * Build an HTTP response.
     *
     * @param  responseBuilder the Rest API response builder
     * @param  httpStatus the status of the response
     * @param  body the response body
     * @return a RestAPIResponse
     */
    RestApiResponse buildResponse(RestApiResponseBuilder responseBuilder, int httpStatus, Serializable body) {
        return responseBuilder.with {
            withResponseStatus(httpStatus)
            withResponse(body)
            build()
        }
    }

    /**
     * Returns a paged result like Bonita BPM REST APIs.
     * Build a response with a content-range.
     *
     * @param  responseBuilder the Rest API response builder
     * @param  body the response body
     * @param  p the page index
     * @param  c the number of result per page
     * @param  total the total number of results
     * @return a RestAPIResponse
     */
    RestApiResponse buildPagedResponse(RestApiResponseBuilder responseBuilder, Serializable body, int p, int c, long total) {
        return responseBuilder.with {
            withContentRange(p,c,total)
            withResponse(body)
            build()
        }
    }

    /**
     * Load a property file into a java.util.Properties
     */
    Properties loadProperties(String fileName, ResourceProvider resourceProvider) {
        Properties props = new Properties()
        resourceProvider.getResourceAsStream(fileName).withStream { InputStream s ->
            props.load s
        }
        props
    }

}

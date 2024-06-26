package com.sgdbf.rest.api

import groovy.json.JsonBuilder

import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.util.List;

import javax.naming.Context
import javax.naming.InitialContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.sql.DataSource

import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.identity.UserMembership
import org.bonitasoft.engine.identity.UserMembershipCriterion
import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.session.APISession
import com.bonitasoft.engine.api.IdentityAPI
import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController
import com.vega_systems.ws.locprotns.ArrayOfSOCIETE
import com.vega_systems.ws.locprotns.LocproPortType
import com.vega_systems.ws.locprotns.LocproServiceBUDGEPOP
import com.vega_systems.ws.locprotns.SOCIETE
import com.vega_systems.ws.locprotns.WSBPRECHERCHERPROPResult

class Index implements RestApiController {

	// CFR 14/12/2020 - Changed log class
	// private static final Logger LOGGER = LoggerFactory.getLogger("org.bonitasoft");
	private static final Logger LOGGER = LoggerFactory.getLogger("com.sgdbf.groovy.restapi.listBudgetProposition");
	
	// Value of role parameter to request no restriction
	private static final String SEARCH_ROLE_WITH_NO_RESTRICTION = 'User';
	
	// Array of Bonita Group names authorized for no restriction
	private static final String[] GROUPS_WITH_NO_RESTRICTION = ["APP_BPM_SGDB_FRANCE_POP3P_USERS", "GROUPE_BO", "GROUPE_LAC", "APP_BPM_SGDB_FRANCE_GEEMCO", "APP_BPM_SGDB_FRANCE_SAV"];
	
	// Lyre message indicating visibility on ALL entities
	private static final String LYRE_MESSAGE_ALL_ENTITIES = "ALL";
	
	private static final String ERR_MSG_NO_VISIBILITY = "Aucune visibilité paramétrée dans Lyre pour l'utilisateur %s";
	private static final String ERR_MSG_WS_FAILED = "Echec de l'interrogation de Lyre";
	
	public class LYRE_VISIBILITY_EXCEPTION extends Exception {
		public LYRE_VISIBILITY_EXCEPTION() {
			super(Index.ERR_MSG_WS_FAILED);
		}
		public LYRE_VISIBILITY_EXCEPTION(Exception ex) {
			super(Index.ERR_MSG_WS_FAILED + " : " + ex.getMessage());
		}
	};
	
	public class LYRE_NO_VISIBILITY_EXCEPTION extends Exception {
		public LYRE_NO_VISIBILITY_EXCEPTION(String sgid) {
			super(String.format(Index.ERR_MSG_NO_VISIBILITY, sgid));
		}
	};
	
	@Override
	RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
		Properties props = loadProperties("configuration.properties", context.resourceProvider)
		def environment= props.getProperty("environment");
		def database= props.getProperty("database");
		def results=[];
		
		// To retrieve query parameters use the request.getParameter(..) method.
		// Be careful, parameter values are always returned as String values

		// Retrieve p parameter
		def p = request.getParameter "p"
		if (p == null) {
			p = "0"
			//return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter p is missing"}""")
		}

		// Retrieve c parameterw
		def c = request.getParameter "c"
		if (c == null) {
			c = Integer.MAX_VALUE.toString()
			//return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter c is missing"}""")
		}
		
		Long nbRes;

		try {
			// CFR 09/09/2021 BPM-656 : Retrieve role parameter and determine entity visibility
			def role = request.getParameter "role";
			def String entityVisibilityCriteria = "";
			if (role == null) {
				return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter role is missing"}""")
			} else {
				APISession session_api = context.getApiSession();
				long bonita_user_id = session_api.getUserId();
				String sgid = session_api.getUserName();
				IdentityAPI identity_api = context.getApiClient().getIdentityAPI();
				entityVisibilityCriteria = getEntityCriteriaForRole(role, bonita_user_id, sgid, props, identity_api);
			}
	
			List<String> filters = request.getParameterMap().get("f")
			List<String> filtersStatus = request.getParameterMap().get("s")
		
			//Gestion de la datasource sur le serveur :
			Context envCtx;
			DataSource ds;

			if ("tomcat".equalsIgnoreCase(environment)){
				envCtx = (Context) new InitialContext();
				ds = (DataSource) envCtx.lookup("java:/comp/env/NotManagedBizDataDS");
			}else if ("jboss".equalsIgnoreCase(environment)){
				InitialContext cxt = new InitialContext();
				envCtx  = (Context)cxt.lookup("java:jboss");
				ds = (DataSource) envCtx.lookup("datasources/NotManagedBizDataDS");
			}
			if ( envCtx == null ) {
				throw new Exception("Error while getting Server context!");
			}
			Connection conn = ds.getConnection();
			Statement requestStatement = conn.createStatement();

			IdentityAPI identityAPI= context.getApiClient().getIdentityAPI();
			User requester=identityAPI.getUser(context.getApiSession().userId);

			StringBuilder strSQLWhere = new StringBuilder(" WHERE p.persistenceId>0 ");

			// CFR 09/09/2021 BPM-656 : Apply entity visibility
			if (entityVisibilityCriteria.length() > 0) {
				strSQLWhere.append(entityVisibilityCriteria);
			}
			
			// Basic filter 
			if (filters  != null) {
				for(e in filters){
					String[] parts = e.split("=");
					String key = parts[0];
					String value = parts[1];
					if (parts.size() > 2){
						String operator = parts[2];
						switch (operator) {
							case 'L':
								// Added by Amit for BPM-40
								if("sitename".equalsIgnoreCase(key))
								{
									strSQLWhere.append(" AND (lower(p.sitename) LIKE lower('%").append(value).append("%') OR lower(p.sitecode) LIKE lower('%").append(value).append("%'))");
								}
								else if("agencyname".equalsIgnoreCase(key))
								{
									strSQLWhere.append(" AND (lower(p.agencyname) LIKE lower('%").append(value).append("%') OR lower(p.agencycode) LIKE lower('%").append(value).append("%'))");
								}
								else if("sectorname".equalsIgnoreCase(key))
								{
									strSQLWhere.append(" AND (lower(p.sectorname) LIKE lower('%").append(value).append("%') OR lower(p.sectorcode) LIKE lower('%").append(value).append("%'))");
								}
								else if("entitename".equalsIgnoreCase(key))
								{
									strSQLWhere.append(" AND (lower(p.entitename) LIKE lower('%").append(value).append("%') OR lower(p.entitecode) LIKE lower('%").append(value).append("%'))");
								}
								else {
									strSQLWhere.append(" AND lower(p."+key+") LIKE lower('%").append(value).append("%')");
								}
								break;
							case 'E':
								strSQLWhere.append(" AND p."+key+"='").append(value).append("'");
								break;
							default :
								strSQLWhere.append(" AND p."+key+" "+ operator + "'").append(value).append("'");
								break;
						}
					} else {
						strSQLWhere.append(" AND p."+key+"='").append(value).append("'");
					}
				}
			}
			
			// Status filter
			// KEYS = A budgéter | A commander | A réceptionner | Terminées
			// Values = ["Afficher", "Masquer"]
			if (filtersStatus  != null) {
				for(e in filtersStatus){
					String[] parts = e.split("=");
					String key = parts[0];
					String value = parts[1];
				
					// BY default we does not remove the proposition if user does not ask them to be hidden
					if(!(value.equalsIgnoreCase("") || value.equalsIgnoreCase("Afficher"))) {
						strSQLWhere.append(" AND p.statusMacroEtapes <>'").append(key).append("'");
					} 
				}
			}
	
			String query= "SELECT * FROM PROPOSITION p" + strSQLWhere.toString() + " ORDER BY persistenceId ASC";
			def offset=Integer.parseInt(p) * Integer.parseInt(c);
			if ("h2".equalsIgnoreCase(database)){
				query += " LIMIT " + c + " OFFSET " + offset;
			}else if ("oracle".equalsIgnoreCase(database)){
				def maxNum=(Integer.parseInt(p)+1) * Integer.parseInt(c);
				def minNum = offset + 1
				query = "SELECT outer.* FROM (SELECT ROWNUM rn, inner.* FROM("+query+") inner) outer WHERE outer.rn >="+minNum+" AND outer.rn<="+maxNum
			}
			else if("mysql".equalsIgnoreCase(database)){
				query += " LIMIT " + offset + "," + c;
			}else{
				throw new Exception("Error while getting Database configuration");
			}


			ResultSet propositions=requestStatement.executeQuery(query);

			while (propositions.next()){
				Map itemProposition=new HashMap();
				
				try {
					User requesterDemande=identityAPI.getUser(propositions.getLong("creatorId"));
					itemProposition.requester=(requesterDemande.firstName?:"") + " " + requesterDemande.lastName;
				} catch (UserNotFoundException e) {
					itemProposition.requester= "User not found";
				}

				itemProposition.budgetYear=propositions.getInt("budgetYear");
				itemProposition.creationDate=propositions.getString("creationDate");
				itemProposition.creatorName=propositions.getString("creatorName");
				itemProposition.materialNumber=propositions.getString("materialNumber");
				
				if (itemProposition.materialNumber.equals("")) {
					itemProposition.materialNumber = "Nouveau materiel - " + propositions.getString("typeMaterialName");
				}

				itemProposition.persistenceId=propositions.getLong("persistenceId");
				
				// BPM-474 : On validation tasks list, the amount displayed is the actualRequestAmount when BudgetProposition has been approved
				// New field initialPropositionAmount added in order to also return the propsitionAmount
				// WAS: itemProposition.propositionAmount=propositions.getDouble("propositionAmount");
				itemProposition.initialPropositionAmount = propositions.getDouble("propositionAmount");
				def Boolean is_budget_approved = propositions.getBoolean("isBudgetPropositionApproved");
				if ((is_budget_approved != null) && (is_budget_approved)) {
					itemProposition.propositionAmount = propositions.getDouble("actualRequestAmount");
					if (itemProposition.propositionAmount == null) {
						itemProposition.propositionAmount = itemProposition.initialPropositionAmount
					}
				} else {
					itemProposition.propositionAmount = itemProposition.initialPropositionAmount
				}
	
				itemProposition.propositionFilter= (propositions.getString("propositionFilter") && propositions.getString("propositionFilter")=="Active")?"Active":""

				itemProposition.agencyName = propositions.getString("agencyCode") + " - " + propositions.getString("agencyName");
				//itemProposition.agencyCode = propositions.getString("agencyCode");

				itemProposition.sectorName = propositions.getString("sectorCode") + " - " + propositions.getString("sectorName");
				//itemProposition.sectorCode = propositions.getString("sectorCode");
				
				itemProposition.siteName = propositions.getString("siteCode") + " - " + propositions.getString("siteName");
				//itemProposition.siteCode = propositions.getString("siteCode");
				
				itemProposition.entiteName = propositions.getString("entiteCode") + " - " + propositions.getString("entiteName");

				itemProposition.territoryName = propositions.getString("territoryCode") + " - " + propositions.getString("territoryName");
				//itemProposition.territoryCode=propositions.getString("territoryCode");

				itemProposition.status=propositions.getString("propositionstatus");
				itemProposition.typeProposition=propositions.getString("typeProposition");
				itemProposition.typeWorkflow=propositions.getString("typeWorkflow");
				itemProposition.statusMacroEtapes=propositions.getString("statusMacroEtapes");
				
				results.add(itemProposition);
			}

			//Count the total number of results
			String countQuery="SELECT COUNT(*) FROM PROPOSITION p "+ strSQLWhere.toString();
			Statement requestStatementCount = conn.createStatement();
			ResultSet nbResSet = requestStatementCount.executeQuery(countQuery);
			while (nbResSet.next()){
				nbRes = nbResSet.getInt(1);
			}

			conn.close();

		} catch (LYRE_NO_VISIBILITY_EXCEPTION e) {
			LOGGER.error("Exception doHandle LYRE_NO_VISIBILITY_EXCEPTION : " + e.getMessage());
			return buildResponse(responseBuilder, HttpServletResponse.SC_FORBIDDEN, new JsonBuilder(e).toPrettyString())
			
		} catch (LYRE_VISIBILITY_EXCEPTION e) {
			LOGGER.error("Exception doHandle LYRE_VISIBILITY_EXCEPTION : " + e.getMessage());
			return buildResponse(responseBuilder, HttpServletResponse.SC_SERVICE_UNAVAILABLE, new JsonBuilder(e).toPrettyString())
			
		} catch (Exception e) {
			LOGGER.error("Exception doHandle : " + e.getMessage());
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST, new JsonBuilder(e).toPrettyString())
		}

		return buildPagedResponse(responseBuilder,  new JsonBuilder(results).toPrettyString(), new Integer(p), new Integer(c), nbRes);
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
	
	private boolean isUserWithNoRestriction(long bonita_user_id, IdentityAPI identity_api) {
		boolean apply_visibility_restriction = true;
		
		try {
			List<UserMembership> memberships = identity_api.getUserMemberships(bonita_user_id, 0, 1000, UserMembershipCriterion.GROUP_NAME_ASC);
			if (memberships != null) {
				int nb_membserhips = memberships.size();
				for (int i=0; i<nb_membserhips; i++) {
					UserMembership one_membership = memberships.get(i);
					String group_name = one_membership.getGroupName();
					if (GROUPS_WITH_NO_RESTRICTION.contains(group_name)) {
						LOGGER.debug("User belongs to group [" + group_name + "] authorized for no restriction");
						apply_visibility_restriction = false;
						break;
					}
				}
			}
			
		} catch(Exception e) {
			LOGGER.error("Exception isUserWithNoRestriction : " + e.getMessage());
		}
		
		return apply_visibility_restriction;
	}
	
	private String checkLyreAuthorization(String sgid, String urlWSDL, String serviceCookie) throws LYRE_VISIBILITY_EXCEPTION{
		String visibility_criteria = "";
		
		try {
			// Get Lyre web service
			LOGGER.debug("Checking Lyre authorizations with URL :" + urlWSDL);
			URL urlBudgePop = new URL(urlWSDL);
			LocproServiceBUDGEPOP budgePopService = new LocproServiceBUDGEPOP(urlBudgePop);
			LocproPortType soapService = budgePopService.getLocproPort();
			
			// Call Lyre web service
			WSBPRECHERCHERPROPResult lyre_authorizations = soapService.wsbpRECHERCHERPROP(serviceCookie, sgid);
			
			// Check Lyre response
			if (lyre_authorizations == null) {
				throw new LYRE_VISIBILITY_EXCEPTION();
				
			} else {
				// Check message
				String lyre_message = lyre_authorizations.getMESSAGE();
				if (LYRE_MESSAGE_ALL_ENTITIES.equalsIgnoreCase(lyre_message)) {
					LOGGER.info("Lyre Entity visibility for user " + sgid + " : " + lyre_message);
				} else {
					// Check the list of entities
					ArrayOfSOCIETE array_entities = lyre_authorizations.getSOCIETES();
					if (array_entities == null) {
						LOGGER.error("ArrayOfSOCIETE is null - No Entity visibility for user " + sgid);
						throw new LYRE_NO_VISIBILITY_EXCEPTION(sgid);

					} else {
						List<SOCIETE> list_entities = array_entities.getSOCIETE();
						int nb_entities = (list_entities == null) ? 0 : list_entities.size();
						LOGGER.debug("Found " + String.valueOf(nb_entities) + " entities in Lyre response");
						if (nb_entities == 0) {
							LOGGER.error("No Entity visibility for user " + sgid);
							throw new LYRE_NO_VISIBILITY_EXCEPTION(sgid);
						} else {
							StringBuilder criteria = new StringBuilder(" AND p.entitecode IN (");
							for (int i=0; i<nb_entities; i++) {
								SOCIETE one_entity = list_entities.get(i);
								String entity_code = one_entity.getCodeNoeud();
								if (entity_code == null) {
									LOGGER.error("NULL entity code for SOCIETE " + String.valueOf(i));
								}
								if (i>0) {
									criteria.append(",");
								}
								criteria.append("'").append(entity_code).append("'");
							}
							criteria.append(")");
							visibility_criteria = criteria.toString();
							LOGGER.info("Entity visibility for user " + sgid + " : " + visibility_criteria);
						}
					}
				}
			}
		} catch (LYRE_NO_VISIBILITY_EXCEPTION e) {
			throw e;
			
		} catch (LYRE_VISIBILITY_EXCEPTION e) {
			throw e;
			
		} catch (Exception e) {
			LOGGER.error("Exception checkLyreAuthorization : " + e.getMessage());
			throw new LYRE_VISIBILITY_EXCEPTION(e);
		}
		
		return visibility_criteria;
	}
	
	private String getEntityCriteriaForRole(String search_role, long bonita_user_id, String sgid, props, IdentityAPI identity_api) {
		String visibility_criteria = "";
		boolean apply_visibility_restriction = true;
		 
		if (SEARCH_ROLE_WITH_NO_RESTRICTION.equalsIgnoreCase(search_role)) {
			LOGGER.debug("Search with role " + search_role + " : no visibility restriction requessted, checking user's memberships to confirm");
			apply_visibility_restriction = isUserWithNoRestriction(bonita_user_id, identity_api);			
		} 
		
		if (apply_visibility_restriction) {
			LOGGER.debug("Search with role " + search_role + " : apply visibility restriction according to Lyre");
			String urlWSDL = props["urlWSDL"];
			String serviceCookie = props["serviceCookie"];
			visibility_criteria = checkLyreAuthorization(sgid, urlWSDL, serviceCookie);
		}
		
		return visibility_criteria;
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

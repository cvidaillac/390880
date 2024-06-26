import org.bonitasoft.engine.identity.User
import com.bonitasoft.engine.api.APIAccessor;


public static getUserLabel (Long userId, APIAccessor apiAccessor) {
	User user=apiAccessor.getIdentityAPI().getUser(userId);
	return (user.firstName ? user.firstName :"") + " " + (user.lastName ? user.lastName :"");
}

public static getUserLabelAndUserName (Long userId, APIAccessor apiAccessor) {
	User user=apiAccessor.getIdentityAPI().getUser(userId);
	return UserDetails.getUserLabel(userId, apiAccessor) + " [" + user.userName + "]";
}

public static UserExist (String userName, APIAccessor apiAccessor) {
	try{
	User user=apiAccessor.getIdentityAPI().getUserByUserName(userName);
	return true;
	}
	catch(Exception e){
		return false;
	}
}

public static UserExistById(long var,apiAccessor){
	try{
	User user=apiAccessor.getIdentityAPI().getUser(var);
	return true;
	}
	catch(Exception e){
		return false;
	}
}

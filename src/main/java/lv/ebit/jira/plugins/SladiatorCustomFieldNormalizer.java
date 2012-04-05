package lv.ebit.jira.plugins;

import static java.lang.String.valueOf;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Date;

import com.atlassian.crowd.embedded.api.User;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.customfields.impl.DateCFType;
import com.atlassian.jira.issue.customfields.impl.DateTimeCFType;
import com.atlassian.jira.issue.customfields.impl.LabelsCFType;
import com.atlassian.jira.issue.customfields.impl.MultiGroupCFType;
import com.atlassian.jira.issue.customfields.impl.MultiSelectCFType;
import com.atlassian.jira.issue.customfields.impl.MultiUserCFType;
import com.atlassian.jira.issue.customfields.impl.ProjectCFType;
import com.atlassian.jira.issue.customfields.impl.UserCFType;
import com.atlassian.jira.issue.customfields.impl.VersionCFType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.project.version.Version;

import com.google.common.base.Joiner;


public class SladiatorCustomFieldNormalizer {
	
	
	public static String getValue(ProjectCFType type, CustomField customField, Issue issue) {
		GenericValue value= transportValueOf(type, customField, issue);
		if (value == null) { return null; }
		return value.getString("key");
	}
	
	public static String getValue(MultiSelectCFType type, CustomField customField, Issue issue) {
		List<String> values = transportValueOf(type, customField, issue);
		if (values == null) { return null; }
		return Joiner.on(", ").skipNulls().join(values);
	}
	
	public static String getValue(DateCFType type, CustomField customField, Issue issue) {
		Date date = (Date) transportValueOf(type, customField, issue);
        if (date == null) { return null; }
        SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd");
        return dateFormat.format(date);
	}
	
	public static String getValue(DateTimeCFType type, CustomField customField, Issue issue) {
		Date date = (Date) transportValueOf(type, customField, issue);
        if (date == null) { return null; }
        SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
	}
	
	public static String getValue(UserCFType type, CustomField customField, Issue issue) {
		User user= (User)transportValueOf(type, customField, issue);
		if (user == null) { return null; }
		return user.getDisplayName();
	}
	
	public static String getValue(MultiUserCFType type, CustomField customField, Issue issue) {
		Collection<User> users = transportValueOf(type, customField, issue);
		if (users == null) { return null; }
		List<String> values = new ArrayList<String>();
		for (User user : users) {
    		values.add(user.getDisplayName());
        }
        return Joiner.on(", ").skipNulls().join(values);
	}
	
	public static String getValue(MultiGroupCFType type, CustomField customField, Issue issue) {
		List<String> values = transportValueOf(type, customField, issue);
		if (values == null) { return null; }
		return Joiner.on(", ").skipNulls().join(values);
	}
	
	public static String getValue(LabelsCFType type, CustomField customField, Issue issue) {
		Collection<String> values = transportValueOf(type, customField, issue);
		if (values == null) { return null; }
		return Joiner.on(", ").skipNulls().join(values);
	}
	public static String getValue(VersionCFType type, CustomField customField, Issue issue) {
		List<Version> values = transportValueOf(type, customField, issue);
        if (values == null) { return null; }

        if (type.isMultiple())
        {
            return Joiner.on(", ").skipNulls().join(values);
        }
        else
        {
            return values.get(0).toString();
        }
	}
	@SuppressWarnings("unchecked")
	public static String getValue(CascadingSelectCFType type, CustomField customField, Issue issue) {
		CustomFieldParams cp = transportValueOf(type, customField, issue);
		if (cp != null) {
			String depth = null;
			List<String> values = new ArrayList<String>();
	        while (cp.containsKey(depth)) {
	        	
				Collection<Object> collection = cp.getValuesForKey(depth);
	        	for (Object opt : collection) {
	        		values.add(opt.toString());
	            }
	        	depth = valueOf(depth == null ? "1" : Integer.valueOf(depth) + 1);
	        }
	        return Joiner.on(" - ").skipNulls().join(values);
		}
		return null;
	}
	
	public static String getValue(CustomFieldType type, CustomField customField, Issue issue) {
//		SladiatorTransport.log.error("name="+type.getClass());
		
		if (type.getClass().getName() == "com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType") {
			return getValue((CascadingSelectCFType)type, customField, issue);
		}
		if (type.getClass().getName() == "com.atlassian.jira.issue.customfields.impl.VersionCFType") {
			return getValue((VersionCFType)type, customField, issue);
		}
		if (type.getClass().getName() == "com.atlassian.jira.issue.customfields.impl.LabelsCFType") {
			return getValue((LabelsCFType)type, customField, issue);
		}
		if (type.getClass().getName() == "com.atlassian.jira.issue.customfields.impl.MultiGroupCFType") {
			return getValue((MultiGroupCFType)type, customField, issue);
		}
		if (type.getClass().getName() == "com.atlassian.jira.issue.customfields.impl.MultiUserCFType") {
			return getValue((MultiUserCFType)type, customField, issue);
		}
		if (type.getClass().getName() == "com.atlassian.jira.issue.customfields.impl.UserCFType") {
			return getValue((UserCFType)type, customField, issue);
		}
		if (type.getClass().getName() == "com.atlassian.jira.issue.customfields.impl.DateTimeCFType") {
			return getValue((DateTimeCFType)type, customField, issue);
		}
		if (type.getClass().getName() == "com.atlassian.jira.issue.customfields.impl.DateCFType") {
			return getValue((DateCFType)type, customField, issue);
		}
		if (type.getClass().getName() == "com.atlassian.jira.issue.customfields.impl.MultiSelectCFType") {
			return getValue((MultiSelectCFType)type, customField, issue);
		}
		if (type.getClass().getName() == "com.atlassian.jira.issue.customfields.impl.ProjectCFType") {
			return getValue((ProjectCFType)type, customField, issue);
		}
		
		if (customField.getValue(issue) != null) {
			return customField.getValue(issue).toString();
		} else {
			return null;
		}
	}
	@SuppressWarnings("unchecked")
	protected static <T> T transportValueOf(CustomFieldType cfType, CustomField customField, Issue issue)
    {
        return (T) cfType.getValueFromIssue(customField, issue);
    }
}

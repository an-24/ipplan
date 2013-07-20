package com.cantor.ipplan.server;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.cantor.ipplan.db.ud.Bargain;
import com.cantor.ipplan.db.ud.Customer;
import com.cantor.ipplan.db.ud.PUserIdent;
import com.cantor.ipplan.db.ud.Task;
import com.google.gdata.client.Query;
import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Person;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.AccessLevelProperty;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.calendar.CalendarFeed;
import com.google.gdata.data.calendar.ColorProperty;
import com.google.gdata.data.calendar.HiddenProperty;
import com.google.gdata.data.extensions.ExtendedProperty;
import com.google.gdata.data.extensions.Reminder;
import com.google.gdata.data.extensions.When;
import com.google.gdata.data.extensions.Where;
import com.google.gdata.util.ResourceNotFoundException;

public class CalendarImport {

	private static final String CAL_IPPLAN_TITLE = "Ipplan";
	public static int CALENDAR_CREATED = 1;
	public static String EXT_GC_CALENDAR_ID = "ipplan-id";
	
	
	private CalendarService service;
	private int lasterr = 0;
	private OAuthToken token;
	private String calendarId;

	private CalendarEntry rootCalendar;
	private CalendarEntry currentCalendar;

	public CalendarImport(OAuthToken token, String calendarId) {
		this.token = token;
		service = new CalendarService("IpplanSyncGoogle");
	    service.setAuthSubToken(token.getValue());
	    this.calendarId = calendarId;
	}
	
	public CalendarEntry getCalendar() throws Exception {
		CalendarEntry cal = null;
		if(calendarId!=null) 
			cal = getIpplanCalendar(calendarId);
		if(cal==null) {
			cal = getIpplanCalendarByTitle();
			if(cal==null) {
				cal = createCalendar();
				lasterr = CALENDAR_CREATED;
			}	
		}	
		return cal;
	}
	
	public List<CalendarEventEntry> getAllEntrys(CalendarEntry cal) throws Exception {
		return getAllEntrys(cal,null);
	}

	public List<CalendarEventEntry> getAllEntrys(CalendarEntry cal, Date lastSync) throws Exception {
		lasterr = 0;
		
		String cid = cal.getId();
		cid = cid.substring(cid.lastIndexOf("/")+1);
		
		URL feedUrl = new URL("https://www.google.com/calendar/feeds/"+cid+"/private/full");
		Query q = new CalendarQuery(feedUrl);
		q.setMaxResults(Integer.MAX_VALUE);
		if(lastSync!=null) 
			q.setUpdatedMin(new DateTime(lastSync));
		
		ArrayList<CalendarEventEntry> result = new ArrayList<CalendarEventEntry>();
		CalendarEventFeed resultFeed = null;
		try {
			resultFeed = service.query(q, CalendarEventFeed.class);
			List<CalendarEventEntry> entrys = resultFeed.getEntries();
			for (CalendarEventEntry entry : entrys) {
				List<ExtendedProperty> eplist = entry.getExtendedProperty();
				for (ExtendedProperty prop : eplist) {
					if(prop.getName().equals(EXT_GC_CALENDAR_ID))
						result.add(entry);
				}
			}
			
		} catch (Exception e) {
			if(!new OAuthService().validateToken(token))
				lasterr = ContactsImport.NO_AUTH_TOKEN;
			else throw e;
		}
		return result; 
	}

	private CalendarEntry getIpplanCalendar(String id) throws Exception {
		URL url = new URL("https://www.google.com/calendar/feeds/default/owncalendars/full");
		try {
			CalendarFeed feed = service.getFeed(url, CalendarFeed.class);
			for (CalendarEntry c: feed.getEntries()) {
				if(c.getAccessLevel().getValue().equals(AccessLevelProperty.OWNER.getValue()))
					rootCalendar = c;
				String cid = c.getId();
				cid = cid.substring(cid.lastIndexOf("/")+1);
				if(cid.equals(id)) return c;
			}
			// для предотврашения рекурсии
			if(rootCalendar==null)
				rootCalendar = feed.getEntries().get(0);
			return null;
		} catch (ResourceNotFoundException e) {
			return null;
		}
	}

	private CalendarEntry getIpplanCalendarByTitle() throws Exception {
		URL url = new URL("https://www.google.com/calendar/feeds/default/owncalendars/full");
		try {
			CalendarFeed feed = service.getFeed(url, CalendarFeed.class);
			for (CalendarEntry c: feed.getEntries()) {
				if(c.getAccessLevel().getValue().equals(AccessLevelProperty.OWNER.getValue()))
					rootCalendar = c;
				if(c.getTitle().equals(CAL_IPPLAN_TITLE)) return c;
			}
			// для предотврашения рекурсии
			if(rootCalendar==null)
				rootCalendar = feed.getEntries().get(0);
			return null;
		} catch (ResourceNotFoundException e) {
			return null;
		}
	}
	
	public int getLastError() {
		return lasterr;
	}

	private CalendarEntry createCalendar() throws Exception {
	    CalendarEntry calendar = new CalendarEntry();
	    calendar.setTitle(new PlainTextConstruct(CAL_IPPLAN_TITLE));
	    calendar.setSummary(new PlainTextConstruct("В этом календаре размещаются задачи связанные со сделками сервиса Ipplan"));
	    calendar.setTimeZone(getRootCalendar().getTimeZone());
	    calendar.setHidden(HiddenProperty.FALSE);
	    calendar.setColor(new ColorProperty("#2952A3"));
	    List<Where> llist = getRootCalendar().getLocations();
	    if(llist!=null && llist.size()>0) calendar.addLocation(llist.get(0));
		URL url = new URL("https://www.google.com/calendar/feeds/default/owncalendars/full");
	    return service.insert(url, calendar);
    }

	public CalendarEntry getRootCalendar() throws Exception {
		return rootCalendar;
	}

	public void addCalendarEvent(Task task,PUserIdent user) throws Exception {
		String calId = user.getPuserGooglecalendarId();
		URL url = new URL("https://www.google.com/calendar/feeds/"+calId+"/private/full");
		CalendarEventEntry entry = new CalendarEventEntry();
		fillEntryFields(task, user, entry);
		service.insert(url, entry);
	}

	public void updateCalendarEvent(CalendarEventEntry entry, Task task, PUserIdent user) throws Exception {
		URL entryUrl = new URL(entry.getEditLink().getHref());
		fillEntryFields(task, user, entry);
		service.update(entryUrl, entry);
	}

	public void deleteCalendarEvent(CalendarEventEntry entry) throws Exception {
		entry.delete();
	}
	private void fillEntryFields(Task task, PUserIdent user,
			CalendarEventEntry entry) throws Exception {

		entry.setTitle(new PlainTextConstruct(task.getTaskName()));
		Bargain bargain = task.getCalendar().getBargain();
		String content = task.getTasktype().getTasktypeName()+"\n";
		content+="сделка: "+bargain.getBargainName()+"\n";
		Customer customer = bargain.getCustomer();
		content+="клиент: "+customer.getCustomerName()+"\n";
		content+="телефоны: "+customer.getCustomerPrimaryPhone();
		if(customer.getCustomerPhones()!=null)		
			content+=", "+customer.getCustomerPhones();
		content+="\n";
		content+="email: "+customer.getCustomerPrimaryEmail();
		if(customer.getCustomerEmails()!=null)		
			content+=", "+customer.getCustomerEmails();
		content+="\n";
		entry.setContent(new PlainTextConstruct(content));
		entry.getAuthors().add(new Person(user.getPuserLogin(),null,null));
		
		When when = new When();
		TimeZone tz = TimeZone.getTimeZone(currentCalendar.getTimeZone().getValue());
		when.setEndTime(new DateTime(task.getTaskDeadline(), tz));
		if(task.getTaskStart()!=null)
			when.setStartTime(new DateTime(task.getTaskStart(), tz)); else
			when.setStartTime(new DateTime(task.getTaskDeadline(), tz));
				
		entry.addTime(when);
		
		if(task.getTaskPlace()!=null) {
			Where where = new Where("", "Место", task.getTaskPlace());
			entry.setExtension(where);
		}
		if(task.getTaskWarningDuration()!=null) {
			Integer value = task.getTaskWarningDuration();
			Reminder reminder = new Reminder();
			switch (task.getTaskWarningUnit()) {
			case 1: reminder.setMinutes(value);
				break;
			case 2: reminder.setHours(value);
				break;
			case 3: reminder.setDays(value);
				break;
			case 4: reminder.setDays(7*value);
				break;
			default:
				throw new Exception("Неизвестная единица измерения в напоминании к задаче");
			}
			entry.setExtension(reminder);
		}
		
		// task id
		ExtendedProperty id = new ExtendedProperty();
		id.setName(EXT_GC_CALENDAR_ID);
		id.setValue(new Integer(task.getTaskId()).toString());
		entry.addExtendedProperty(id);
		
		// не работает! API 2 не принимает colorId 
//		if(task.getTaskExecuted()!=0)
//			entry.setExtension(new ColorIdProperty("4"));
		
		
		// status?
		//entry.setStatus(new EventStatus());
	}

	public void setCurrentCalendar(CalendarEntry cal) {
		currentCalendar = cal;
	}
/*	
	static class ColorIdProperty extends ValueConstruct {

		public static ExtensionDescription getDefaultDescription() {
		    return new ExtensionDescription(ColorIdProperty.class,
			        Namespaces.gCalNs, "colorId");
	    }
		
		protected ColorIdProperty() {
			this(null);
		}
		
		protected ColorIdProperty(String value) {
		    super(Namespaces.gCalNs, "colorId", "value", value);
		}
		
	}
*/	
}

package client.gui;

import io.ReadForexCalendar;

import java.awt.Color;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;

import trading.util.Event;
import trading.util.Event.EventImpact;
import trading.util.Event.EventResult;

/**
 * UI for upcoming forex events. Only lists events pre-today.
 * 
 * TODO ta bort statiska skit
 * 
 * @author Tobias W
 * 
 */
@SuppressWarnings("serial")
public class UpcomingEvents extends JScrollPane {

	private final DefaultListModel listModel;
	private final JList eventsList;
	private ArrayList<StringBuilder> eventsStringList;
	private ReadForexCalendar rfc;
	// private final StrategyController strategyController;

	private Event upcomingEvent;
	private final ArrayList<Event> upcomingEvents = new ArrayList<Event>();

	private boolean updating = false;

	public UpcomingEvents() {
		// this.strategyController = strategyController;
		listModel = new DefaultListModel();
		eventsList = new JList();
		eventsList.setModel(listModel);
		eventsList.setFixedCellHeight(15);
		// eventsList.setFont(new Font("helvetica", Font.PLAIN, 18));

		eventsList.setSelectionModel(new DefaultListSelectionModel() {
			@Override
			public void setSelectionInterval(int index0, int index1) {
				if (eventsList.isSelectedIndex(index0)) {
					eventsList.removeSelectionInterval(index0, index1);
				} else {
					eventsList.addSelectionInterval(index0, index1);
				}
			}
		});

		this.add(eventsList);
		this.getViewport().setView(eventsList);
		this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		// set border
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

		downloadEvents();

		checkEventDatesForUpcoming();
		eventsList.setCellRenderer(new ListColorRenderer());
		eventsList.setForeground(Color.WHITE);

	}

	// This method assumes that the XML file has times given 2 hours behind the
	// client time.
	// 3 am here means 1 am in the XML file.
	private void checkEventDatesForUpcoming() {
		Pattern dateMatcher = Pattern
				.compile("[0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9]");
		Pattern clockMatcher = Pattern
				.compile("[0-9]?[0-9]:[0-9][0-9][a-z][a-z]");
		Pattern pmMatcher = Pattern.compile("[0-9]?[0-9]:[0-9][0-9]pm");

		DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy hh:mm");
		DateFormat newDateFormat = new SimpleDateFormat("yy-MM-dd HH:mm");

		Date date = new Date();
		Calendar calEvent = new GregorianCalendar();

		int counter = 0;
		// DefaultListModel newListModel = new DefaultListModel();
		for (StringBuilder sb : eventsStringList) {

			Matcher matcher = dateMatcher.matcher(sb);
			Matcher matcher2 = clockMatcher.matcher(sb);
			String eventString = "";
			if (matcher.find() && matcher2.find()) {
				try {
					date = dateFormat.parse(matcher.group() + " "
							+ matcher2.group());
					matcher = pmMatcher.matcher(sb);
					if (matcher.find())
						calEvent.setTimeInMillis(date.getTime() + 43200000);
					else
						calEvent.setTime(date);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				// if event is upcoming and should be added to the list
				if (System.currentTimeMillis()
						- (calEvent.getTimeInMillis() + 3600000 * 2) < 0) {
					eventString = sb.toString();

					calEvent.add(Calendar.HOUR_OF_DAY, 2);
					String newDateString = newDateFormat.format(new Date(
							calEvent.getTimeInMillis()));
					eventString = eventString
							.replaceFirst(
									"[0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9] \\| [0-9]?[0-9]:[0-9][0-9][a-z][a-z]",
									newDateString);
					listModel.addElement(eventString);

					if (counter == 0) {

						upcomingEvent = new Event(getTitle(eventString),
								EventResult.NONE, getInstrument(eventString),
								getImpact(eventString),
								calEvent.getTimeInMillis());

						upcomingEvents.clear();
						upcomingEvents.add(upcomingEvent);

					}

					// add event to upcoming array if it happens at the same
					// time as the first event
					if (counter != 0
							&& upcomingEvent.getTime() == calEvent
									.getTimeInMillis()) {
						Event event = new Event(getTitle(eventString));

						// Sometimes a event can affect several currencies. No
						// need to add both.
						if (!event.getTitle().equals(upcomingEvent.getTitle())) {
							event.setTime(calEvent.getTimeInMillis());
							event.setInstrument(getInstrument(eventString));
							event.setImpact(getImpact(eventString));
							upcomingEvents.add(event);
						}

					}
					counter++;
				}

			}

		}
		// eventsList.setModel(newListModel);
		eventsList.repaint();
		this.repaint();
		// System.out.println("Antal upcoming events n?sta h?llplats: "
		// + upcomingEvents.size());
	}

	private String getTitle(String eventString) {
		Pattern pattern = Pattern
				.compile("^([A-Z]*/?-?[a-z]*[A-Z]* * ([A-Z]*[a-z]*-?[A-Z]?[a-z]* ?)* ?[a-z]*/*[a-z]*)");
		Matcher matcher = pattern.matcher(eventString);

		String eventTitle = "";
		if (matcher.find())
			eventTitle = matcher.group().replaceAll("[ \t]+$", "");

		return eventTitle;
	}

	private String getInstrument(String eventString) {
		Pattern pattern = Pattern.compile("\\| [A-Z][A-Z][A-Z] \\|");
		Matcher matcher = pattern.matcher(eventString);

		String instrument = "";
		if (matcher.find())
			instrument = matcher.group().replaceAll("[ \t]+$", "");
		instrument = instrument.replaceAll(" ", "");
		instrument = instrument.replaceAll("\\|", "");
		return instrument;
	}

	private EventImpact getImpact(String eventString) {
		Pattern pattern = Pattern.compile("\\| [A-Z][a-z]* \\|");
		Matcher matcher = pattern.matcher(eventString);

		String impact = "";
		if (matcher.find())
			impact = matcher.group().replaceAll("[ \t]+$", "");

		if (impact.contains("High"))
			return EventImpact.HIGH;
		else if (impact.contains("Low"))
			return EventImpact.LOW;
		else
			return EventImpact.MEDIUM;
	}

	/**
	 * Updates the events (so past events doesn't show)
	 */
	public void updateEvents(boolean parseXML) {
		if (!updating) {
			updating = true;
			listModel.clear();
			if (parseXML)
				eventsStringList = rfc.parseWithXPath("/weeklyevents/event");

			new CheckEventDatesTask().execute();
		}
	}

	private class CheckEventDatesTask extends SwingWorker<Void, Integer> {

		public CheckEventDatesTask() {

		}

		@Override
		protected void done() {
			updating = false;
		}

		@Override
		protected Void doInBackground() throws Exception {
			checkEventDatesForUpcoming();

			// TODO
			// if M?ndag morgon + eventslistan ?r tom, downloadEvents()
			// ?ndra till s?ndag kl 21.00 kanske, d? b?rjar ticken ramla in
			Calendar now = Calendar.getInstance();
			if (now.get(Calendar.DAY_OF_WEEK) == 1 && upcomingEvents.isEmpty()) {
				System.out.println("- Downloading new events for the week -");
				downloadEvents();
			}

			return null;
		}

	}

	/**
	 * Downloads a weekly event xml file.
	 */
	public void downloadEvents() {
		rfc = new ReadForexCalendar("calendar.xml");
		rfc.downloadFile("http://www.forexfactory.com/ffcal_week_this.xml");
		eventsStringList = rfc.parseWithXPath("/weeklyevents/event");

	}

	public Event getUpcomingEvent() {
		return upcomingEvent;
	}

	public ArrayList<Event> getUpcomingEvents() {
		return upcomingEvents;
	}

	public DefaultListModel getListModel() {
		return listModel;
	}

}

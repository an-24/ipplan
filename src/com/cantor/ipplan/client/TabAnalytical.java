package com.cantor.ipplan.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.cantor.ipplan.client.widgets.CheckBox;
import com.cantor.ipplan.client.widgets.DropdownButton;
import com.cantor.ipplan.client.widgets.HorizontalPanel;
import com.cantor.ipplan.client.widgets.MonthPicker;
import com.cantor.ipplan.client.widgets.VerticalPanel;
import com.cantor.ipplan.shared.ChartOptions;
import com.cantor.ipplan.shared.DistributeCost;
import com.cantor.ipplan.shared.DistributeStaff;
import com.cantor.ipplan.shared.DynamicMonthData;
import com.cantor.ipplan.shared.DynamicMonthSeries;
import com.cantor.ipplan.shared.StatusWrapper;
import com.cantor.ipplan.shared.Utils;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.ShowRangeEvent;
import com.google.gwt.event.logical.shared.ShowRangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.googlecode.gflot.client.DataPoint;
import com.googlecode.gflot.client.PieDataPoint;
import com.googlecode.gflot.client.PlotModel;
import com.googlecode.gflot.client.Series;
import com.googlecode.gflot.client.SeriesHandler;
import com.googlecode.gflot.client.SimplePlot;
import com.googlecode.gflot.client.Tick;
import com.googlecode.gflot.client.jsni.JsonObject;
import com.googlecode.gflot.client.options.AxisOptions;
import com.googlecode.gflot.client.options.BarSeriesOptions;
import com.googlecode.gflot.client.options.BarSeriesOptions.BarAlignment;
import com.googlecode.gflot.client.options.GlobalSeriesOptions;
import com.googlecode.gflot.client.options.LegendOptions;
import com.googlecode.gflot.client.options.LegendOptions.LegendPosition;
import com.googlecode.gflot.client.options.PieSeriesOptions;
import com.googlecode.gflot.client.options.PieSeriesOptions.Label.Formatter;
import com.googlecode.gflot.client.options.PieSeriesOptions.Stroke;
import com.googlecode.gflot.client.options.PlotOptions;

public class TabAnalytical extends InplaceForm {

	protected static final int PLOT_COORD_ROW = 2;
	protected static final int PLOT_COORD_COLUMN = 0;
    private static final String[] MONTH_NAMES = {"январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август", 
    	                                         "сентябрь", "октябрь", "ноябрь", "декабрь"};
	private Button btnRefresh;
	private MonthPicker mpStart;
	private MonthPicker mpFinish;
	private DropdownButton dbTypeReport;
	private int currentReport;
	private Date dStart;
	private Date dFinish;
	private AnalyticalServiceAsync service;
	private HashMap<ToggleButton, StatusWrapper> toggles;
	private HorizontalPanel hpToggles;
	private CheckBox cbAll;
	private CheckBox cbExcludeSelf;
	
	private static final int REPORT_SALES_DYN = 1;
	private static final int REPORT_DISTR_STAFF = 2;
	private static final int REPORT_EFFECTIVE_SALES = 3;
	private static final int REPORT_DISTR_COST = 4;
	private static final int REPORT_EXEC_DYN = 5;
	private static final int REPORT_FINANCE_DYN = 6;
	
	static private class ReportInfo {
		int key;
		String name;
		
		ReportInfo(int key, String name) {
			this.key = key;
			this.name = name;
		}
	}
	
	private static final List<ReportInfo> REPORTS = new ArrayList<ReportInfo>();
	static {
		REPORTS.add(new ReportInfo(REPORT_EXEC_DYN, "Димамика выполнения сделок"));
		REPORTS.add(new ReportInfo(REPORT_DISTR_COST, "Распределение затрат"));
		REPORTS.add(new ReportInfo(REPORT_FINANCE_DYN, "Димамика финансовых показателей"));
		REPORTS.add(new ReportInfo(0, "Продажи"));
		REPORTS.add(new ReportInfo(REPORT_SALES_DYN, "Динамика продаж"));
		REPORTS.add(new ReportInfo(REPORT_DISTR_STAFF, "Распределение по подчиненным"));
		REPORTS.add(new ReportInfo(REPORT_EFFECTIVE_SALES, "Эффективные продавцы"));
	}
	
	public TabAnalytical(FormMain form) {
		super();
		setHeight("100%");
		init();
	}
	
	private AnalyticalServiceAsync getService() {
		if(service!=null) return service;
		service = GWT.create(AnalyticalService.class);
		return service;
	}

	private void init() {
		int col = 0, row = 0;

		dStart = new Date();
		dStart.setMonth(0);
		dStart.setDate(1);
		
		dFinish = new Date();
		dFinish.setDate(1);
		CalendarUtil.addMonthsToDate(dFinish, 1);
		CalendarUtil.addDaysToDate(dFinish, -1);
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		hp.setSpacing(10);
		btnRefresh = new Button("Обновить");
		btnRefresh.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
		});
		hp.add(btnRefresh);
		
		dbTypeReport = new DropdownButton("<Вид отчета>");
		fillReportTypes();
		hp.add(dbTypeReport);
		
		setWidget(row, col, hp);
		
		row++;
		
	}
	
	private void fillReportTypes() {
		
		class ReportScheduledCommand implements Scheduler.ScheduledCommand {
			private ReportInfo report;
			ReportScheduledCommand(ReportInfo report) {
				this.report = report;
			}

			@Override
			public void execute() {
				setReportType(report);
				dbTypeReport.closeup();
			}
			
		};
		
		for (ReportInfo r : REPORTS) {
			if(r.key==0) dbTypeReport.getMenu().addSeparator(); 
				  else dbTypeReport.getMenu().addItem(new MenuItem(r.name,new ReportScheduledCommand(r)));
		}
/*		
		dbTypeReport.getMenu().addItem(new MenuItem(REPORTS.get(REPORT_SALES_DYN),new ReportScheduledCommand(REPORT_SALES_DYN)));
		dbTypeReport.getMenu().addItem(new MenuItem(REPORTS.get(REPORT_DISTR_STAFF),new ReportScheduledCommand(REPORT_DISTR_STAFF)));
*/		
	}
	
	public void setReportType(ReportInfo report) {
		currentReport =  report.key;
		dbTypeReport.setText(report.name);
		// формируем панель параметров
		switch (currentReport) {
			case REPORT_SALES_DYN: {
				VerticalPanel vp = new VerticalPanel();
				HorizontalPanel hp = new HorizontalPanel();
				hp.setSpacing(5);
				hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
				
				hp.add(new Label("Начиная с "));
				mpStart = new MonthPicker(dStart);
				mpStart.addShowRangeHandler(new ShowRangeHandler<Date>() {
					@Override
					public void onShowRange(ShowRangeEvent<Date> event) {
						dStart =  event.getStart();
						resetCurrentReport();
					}
				});
				hp.add(mpStart);
				hp.add(new Label(" до конца месяца "));
				mpFinish = new MonthPicker(dFinish);
				mpFinish.addShowRangeHandler(new ShowRangeHandler<Date>() {
					@Override
					public void onShowRange(ShowRangeEvent<Date> event) {
						dFinish =  event.getEnd();
						resetCurrentReport();
					}
				});
				hp.add(mpFinish);
				
				cbAll = new CheckBox("Сделки подчиненных");
				cbAll.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						resetCurrentReport();
					}
				});
				cbAll.setValue(true);
				hp.add(cbAll);
				cbExcludeSelf = new CheckBox("Исключить свои");
				cbExcludeSelf.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						resetCurrentReport();
					}
				});
				hp.add(cbExcludeSelf);
				
				vp.add(hp);
				
				hpToggles = new HorizontalPanel();
				ArrayList<StatusWrapper> values = new ArrayList<StatusWrapper>();
				toggles = new HashMap<ToggleButton, StatusWrapper>();
				values.addAll(StatusWrapper.getAllStatuses().values());
				Collections.sort(values, new Comparator<StatusWrapper>() {
					@Override
					public int compare(StatusWrapper arg0, StatusWrapper arg1) {
						return arg0.statusId>arg1.statusId?1:arg0.statusId<arg1.statusId?-1:0;
					}
				});
				for (StatusWrapper st : values) {
					if(st.statusId<=StatusWrapper.EXECUTION || st.statusId==StatusWrapper.CLOSE_FAULT) { // только продажи
						final ToggleButton toogle = new ToggleButton(st.statusName);
						final Element el = DOM.createDiv();
						el.setClassName("anl-status-legend");
						el.getStyle().setBackgroundColor(StatusWrapper.getBackgroundColor(st.statusId));
						toogle.addAttachHandler(new Handler() {
							@Override
							public void onAttachOrDetach(AttachEvent event) {
								toogle.getElement().getParentElement().appendChild(el);
							}
						});
						
						hpToggles.add(toogle);
						toggles.put(toogle, st);
						if(st.statusId==StatusWrapper.EXECUTION)   //default
							toogle.setDown(true);
						toogle.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								resetCurrentReport();
							}
						});
					}
				}
				vp.add(hpToggles);
				
				setWidget(1, 0, vp);
			}
			break;
			
			case REPORT_DISTR_STAFF: {
				VerticalPanel vp = new VerticalPanel();
				HorizontalPanel hp = new HorizontalPanel();
				hp.setSpacing(5);
				hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
				
				hp.add(new Label("Начиная с "));
				mpStart = new MonthPicker(dStart);
				mpStart.addShowRangeHandler(new ShowRangeHandler<Date>() {
					@Override
					public void onShowRange(ShowRangeEvent<Date> event) {
						dStart =  event.getStart();
						resetCurrentReport();
					}
				});
				hp.add(mpStart);
				hp.add(new Label(" до конца месяца "));
				mpFinish = new MonthPicker(dFinish);
				mpFinish.addShowRangeHandler(new ShowRangeHandler<Date>() {
					@Override
					public void onShowRange(ShowRangeEvent<Date> event) {
						dFinish =  event.getEnd();
						resetCurrentReport();
					}
				});
				hp.add(mpFinish);

				cbAll = new CheckBox("Сделки подчиненных");
				cbAll.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						resetCurrentReport();
					}
				});
				cbAll.setValue(true);
				hp.add(cbAll);
				cbExcludeSelf = new CheckBox("Исключить свои");
				cbExcludeSelf.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						resetCurrentReport();
					}
				});
				hp.add(cbExcludeSelf);
				
				vp.add(hp);
				
				hpToggles = new HorizontalPanel();
				ArrayList<StatusWrapper> values = new ArrayList<StatusWrapper>();
				toggles = new HashMap<ToggleButton, StatusWrapper>();
				values.addAll(StatusWrapper.getAllStatuses().values());
				Collections.sort(values, new Comparator<StatusWrapper>() {
					@Override
					public int compare(StatusWrapper arg0, StatusWrapper arg1) {
						return arg0.statusId>arg1.statusId?1:arg0.statusId<arg1.statusId?-1:0;
					}
				});
				for (StatusWrapper st : values) {
					if(st.statusId<=StatusWrapper.EXECUTION || st.statusId==StatusWrapper.CLOSE_FAULT) { // только продажи
						final ToggleButton toogle = new ToggleButton(st.statusName);
						final Element el = DOM.createDiv();
						el.setClassName("anl-status-legend");
						el.getStyle().setBackgroundColor(StatusWrapper.getBackgroundColor(st.statusId));
						toogle.addAttachHandler(new Handler() {
							@Override
							public void onAttachOrDetach(AttachEvent event) {
								toogle.getElement().getParentElement().appendChild(el);
							}
						});
						
						hpToggles.add(toogle);
						toggles.put(toogle, st);
						if(st.statusId==StatusWrapper.EXECUTION)   //default
							toogle.setDown(true);
						toogle.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								ToggleButton currtoggle = (ToggleButton) event.getSource();
								for (ToggleButton btn : toggles.keySet()) {
									if(currtoggle!=btn) btn.setDown(false);
								}
								resetCurrentReport();
							}
						});
					}
				}
				vp.add(hpToggles);
				setWidget(1, 0, vp);
			}
			break;
			case REPORT_EFFECTIVE_SALES: {
				VerticalPanel vp = new VerticalPanel();
				HorizontalPanel hp = new HorizontalPanel();
				hp.setSpacing(5);
				hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
				
				hp.add(new Label("Начиная с "));
				mpStart = new MonthPicker(dStart);
				mpStart.addShowRangeHandler(new ShowRangeHandler<Date>() {
					@Override
					public void onShowRange(ShowRangeEvent<Date> event) {
						dStart =  event.getStart();
						resetCurrentReport();
					}
				});
				hp.add(mpStart);
				hp.add(new Label(" до конца месяца "));
				mpFinish = new MonthPicker(dFinish);
				mpFinish.addShowRangeHandler(new ShowRangeHandler<Date>() {
					@Override
					public void onShowRange(ShowRangeEvent<Date> event) {
						dFinish =  event.getEnd();
						resetCurrentReport();
					}
				});
				hp.add(mpFinish);
				
				vp.add(hp);
				
				setWidget(1, 0, vp);
				
			}
			break;
			case REPORT_DISTR_COST: {
				VerticalPanel vp = new VerticalPanel();
				HorizontalPanel hp = new HorizontalPanel();
				hp.setSpacing(5);
				hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
				
				hp.add(new Label("Начиная с "));
				mpStart = new MonthPicker(dStart);
				mpStart.addShowRangeHandler(new ShowRangeHandler<Date>() {
					@Override
					public void onShowRange(ShowRangeEvent<Date> event) {
						dStart =  event.getStart();
						resetCurrentReport();
					}
				});
				hp.add(mpStart);
				hp.add(new Label(" до конца месяца "));
				mpFinish = new MonthPicker(dFinish);
				mpFinish.addShowRangeHandler(new ShowRangeHandler<Date>() {
					@Override
					public void onShowRange(ShowRangeEvent<Date> event) {
						dFinish =  event.getEnd();
						resetCurrentReport();
					}
				});
				hp.add(mpFinish);
				
				cbAll = new CheckBox("Сделки подчиненных");
				cbAll.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						resetCurrentReport();
					}
				});
				cbAll.setValue(true);
				hp.add(cbAll);
				cbExcludeSelf = new CheckBox("Исключить свои");
				cbExcludeSelf.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						resetCurrentReport();
					}
				});
				hp.add(cbExcludeSelf);
				
				vp.add(hp);
				
				setWidget(1, 0, vp);
			}
			break;

			case REPORT_EXEC_DYN: {
				VerticalPanel vp = new VerticalPanel();
				HorizontalPanel hp = new HorizontalPanel();
				hp.setSpacing(5);
				hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
				
				hp.add(new Label("Начиная с "));
				mpStart = new MonthPicker(dStart);
				mpStart.addShowRangeHandler(new ShowRangeHandler<Date>() {
					@Override
					public void onShowRange(ShowRangeEvent<Date> event) {
						dStart =  event.getStart();
						resetCurrentReport();
					}
				});
				hp.add(mpStart);
				hp.add(new Label(" до конца месяца "));
				mpFinish = new MonthPicker(dFinish);
				mpFinish.addShowRangeHandler(new ShowRangeHandler<Date>() {
					@Override
					public void onShowRange(ShowRangeEvent<Date> event) {
						dFinish =  event.getEnd();
						resetCurrentReport();
					}
				});
				hp.add(mpFinish);
				
				cbAll = new CheckBox("Сделки подчиненных");
				cbAll.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						resetCurrentReport();
					}
				});
				cbAll.setValue(true);
				hp.add(cbAll);
				cbExcludeSelf = new CheckBox("Исключить свои");
				cbExcludeSelf.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						resetCurrentReport();
					}
				});
				hp.add(cbExcludeSelf);
				
				vp.add(hp);
				
				hpToggles = new HorizontalPanel();
				ArrayList<StatusWrapper> values = new ArrayList<StatusWrapper>();
				toggles = new HashMap<ToggleButton, StatusWrapper>();
				values.addAll(StatusWrapper.getAllStatuses().values());
				Collections.sort(values, new Comparator<StatusWrapper>() {
					@Override
					public int compare(StatusWrapper arg0, StatusWrapper arg1) {
						return arg0.statusId>arg1.statusId?1:arg0.statusId<arg1.statusId?-1:0;
					}
				});
				for (StatusWrapper st : values) {
					if(st.statusId>=StatusWrapper.EXECUTION) {
						final ToggleButton toogle = new ToggleButton(st.statusName);
						final Element el = DOM.createDiv();
						el.setClassName("anl-status-legend");
						el.getStyle().setBackgroundColor(StatusWrapper.getBackgroundColor(st.statusId));
						toogle.addAttachHandler(new Handler() {
							@Override
							public void onAttachOrDetach(AttachEvent event) {
								toogle.getElement().getParentElement().appendChild(el);
							}
						});
						
						hpToggles.add(toogle);
						toggles.put(toogle, st);
						if(st.statusId==StatusWrapper.EXECUTION)   //default
							toogle.setDown(true);
						toogle.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								resetCurrentReport();
							}
						});
					}
				}
				vp.add(hpToggles);
				
				setWidget(1, 0, vp);
			}
			break;
			
			case REPORT_FINANCE_DYN: {
				VerticalPanel vp = new VerticalPanel();
				HorizontalPanel hp = new HorizontalPanel();
				hp.setSpacing(5);
				hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
				
				hp.add(new Label("Начиная с "));
				mpStart = new MonthPicker(dStart);
				mpStart.addShowRangeHandler(new ShowRangeHandler<Date>() {
					@Override
					public void onShowRange(ShowRangeEvent<Date> event) {
						dStart =  event.getStart();
						resetCurrentReport();
					}
				});
				hp.add(mpStart);
				hp.add(new Label(" до конца месяца "));
				mpFinish = new MonthPicker(dFinish);
				mpFinish.addShowRangeHandler(new ShowRangeHandler<Date>() {
					@Override
					public void onShowRange(ShowRangeEvent<Date> event) {
						dFinish =  event.getEnd();
						resetCurrentReport();
					}
				});
				hp.add(mpFinish);
				
				cbAll = new CheckBox("Сделки подчиненных");
				cbAll.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						resetCurrentReport();
					}
				});
				cbAll.setValue(true);
				hp.add(cbAll);
				cbExcludeSelf = new CheckBox("Исключить свои");
				cbExcludeSelf.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						resetCurrentReport();
					}
				});
				hp.add(cbExcludeSelf);
				
				vp.add(hp);
				
				setWidget(1, 0, vp);
			}
			break;

		default:
			break;
		}
		refresh();
	}
	
	protected void resetCurrentReport() {
        setWidget(PLOT_COORD_ROW, PLOT_COORD_COLUMN, null);
	}

	protected void refresh() {
		resetErrors();
		switch (currentReport) {
			
			case REPORT_SALES_DYN: {
				ArrayList<Integer> statuses = new ArrayList<Integer>();
				for (ToggleButton toggle : toggles.keySet()) {
					if(toggle.isDown()) 
						statuses.add(toggles.get(toggle).statusId);
				}
				if(statuses.size()==0) {
					showError(this, 2, "Необходимо выбрать статус");
					return;
				}

				ChartOptions options = new ChartOptions();
				options.all = cbAll.getValue();
				options.excludeSelf = cbExcludeSelf.getValue();
				
				getService().dynamicMonthData(dStart, dFinish, statuses.toArray(new Integer[]{}), options, new AsyncCallback<List<DynamicMonthSeries>>() {
					
					@Override
					public void onSuccess(final List<DynamicMonthSeries> result) {
						PlotOptions plotOptions = PlotOptions.create();
						
						double commonWidth = 0.7;
						double barWidth = commonWidth*1.0/result.size();
						
						BarSeriesOptions barOpt = BarSeriesOptions.create()
								.setShow(true)
								.setLineWidth(1)
								.setBarWidth(barWidth-0.05)
								.setFill(0.7)
								.setAlignment(BarAlignment.CENTER);
						GlobalSeriesOptions options = GlobalSeriesOptions.create().setBarsSeriesOptions(barOpt);
						plotOptions.setGlobalSeriesOptions(options);
						plotOptions.setLegendOptions(LegendOptions.create()
								//.setBackgroundColor("#FFFFF0")
								.setBackgroundOpacity(0)
								.setPosition(LegendPosition.NORTH_WEST)
								.setShow(true));
						// нужно взять кол-во точек, берем у первой серии
						final DynamicMonthSeries firstseria = result.size()>0?result.get(0):null;
						int pointcount = 0;
						if(result.size()>0) 
							pointcount = firstseria.data.size();
						// axis X
						if(firstseria!=null) {
							AxisOptions xOpt = AxisOptions.create().setTicks(pointcount);
							JsArray<Tick> ticks = JavaScriptObject.createArray().cast();
							for (DynamicMonthData d : firstseria.data) {
			            		String s = MONTH_NAMES[d.getMonth()-1];
			            		if(d.getMonth()==1) s+=",\n"+d.getYear()+" г.";
								ticks.push(Tick.of(d.getYear()*12+d.getMonth(),s));
							}
							xOpt.setTicks(ticks);
							plotOptions.addXAxisOptions(xOpt);
						}
						// axis Y
						AxisOptions yOpt =  AxisOptions.create()
								.setMinTickSize(1)
								.setLabel("Кол-во сделок");
						plotOptions.addYAxisOptions(yOpt);
						
					    // готовим серии
						PlotModel model = new PlotModel();
						double offs = (result.size()-1)*barWidth/2.0;
					    for (DynamicMonthSeries seria : result) {
					    	StatusWrapper sw = StatusWrapper.getAllStatuses().get(seria.statusId);
					    	Series graph = Series.of(sw.statusName)
					    			.setColor(StatusWrapper.getBackgroundColor(seria.statusId));
					    	graph.put("borderColor", "#939393");
					        SeriesHandler handler = model.addSeries(graph);
					        for (DynamicMonthData d : seria.data) {
						        handler.add(DataPoint.of(d.getYear()*12+d.getMonth()+offs, d.getCount()));
							}
					        offs-=barWidth;
						}
					    // draw
				        SimplePlot plot = new SimplePlot(model,plotOptions);
				        plot.setHeight(590);
				        plot.setWidth(769);
				        
				        setWidget(PLOT_COORD_ROW, PLOT_COORD_COLUMN, plot);
				        getFlexCellFormatter().setHorizontalAlignment(PLOT_COORD_ROW, PLOT_COORD_COLUMN, HasHorizontalAlignment.ALIGN_CENTER);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						Ipplan.showError(caught);
					}
				});
				
			};	
			break;
			
			case REPORT_DISTR_STAFF: {
				int st = 0; 
				for (ToggleButton toggle : toggles.keySet()) {
					if(toggle.isDown()) {
						st = toggles.get(toggle).statusId;
						break;
					}	
				}
				ChartOptions options = new ChartOptions();
				options.all = cbAll.getValue();
				options.excludeSelf = cbExcludeSelf.getValue();
				
				getService().distributeStaffs(dStart, dFinish, st, options, new AsyncCallback<List<DistributeStaff>>() {
					
					@Override
					public void onSuccess(List<DistributeStaff> result) {
						PlotOptions plotOptions = PlotOptions.create();
						PieSeriesOptions opt = PieSeriesOptions.create()
								.setRadius(0.9)
								//.setStartAngle(0.6)
								//.setOffset(Offset.of(30, 30))
								.setTilt(0.5)
								.setStroke(Stroke.create().setWidth(2))
								.setLabel(com.googlecode.gflot.client.options.PieSeriesOptions.Label.create()
										.setFormatter(new Formatter() {
											@Override
											public String format(String label, Series series) {
												int data = (int)series.getData().getY(0);
												String sufx = Utils.getNumberPadeg(new String[]{"сделка","сделки","сделок"},data);												
												return  label+", "+
														NumberFormat.getFormat("##0.0").format(series.getPercent())+
														"% ("+data +" "+sufx+")";
											}
										})
										.setShow(true)
										)
								.setShow(true); 
						plotOptions.setGlobalSeriesOptions(GlobalSeriesOptions.create().setPieSeriesOptions(opt));
					    // готовим серии
						PlotModel model = new PlotModel();
					    for (DistributeStaff data : result) {
					    	Series graph = Series.of(data.getUserLogin());
					        SeriesHandler handler = model.addSeries(graph);
					        handler.add(PieDataPoint.of(data.getInt()));
						}
				        SimplePlot plot = new SimplePlot(model,plotOptions);
				        plot.setHeight(590);
				        plot.setWidth(769);
				        
				        setWidget(PLOT_COORD_ROW, PLOT_COORD_COLUMN, plot);
				        getFlexCellFormatter().setHorizontalAlignment(PLOT_COORD_ROW, PLOT_COORD_COLUMN, HasHorizontalAlignment.ALIGN_CENTER);
						
					}
					
					@Override
					public void onFailure(Throwable caught) {
						Ipplan.showError(caught);
					}
				});
				
			}
			break;
			case REPORT_EFFECTIVE_SALES: {
				ChartOptions options = new ChartOptions();
				getService().effectiveSales(dStart, dFinish, options, new AsyncCallback<List<DistributeStaff>>() {
					
					@Override
					public void onSuccess(List<DistributeStaff> result) {
						PlotOptions plotOptions = PlotOptions.create();
						
						BarSeriesOptions barOpt = BarSeriesOptions.create()
								.setHorizontal(true)
								.setShow(true)
								.setLineWidth(3)
								.setBarWidth(0.3)
								//.setFill(0.7)
								.setAlignment(BarAlignment.CENTER);
						plotOptions.setGlobalSeriesOptions(GlobalSeriesOptions.create().setBarsSeriesOptions(barOpt));
						plotOptions.setLegendOptions(LegendOptions.create()
								.setShow(false));
						// axis Y
						AxisOptions yOpt =  AxisOptions.create()
								.setMinTickSize(1)
								.setMaximum(result.size()+1)
								.setLabel("Продавцы");
						JsArray<Tick> ticks = JavaScriptObject.createArray().cast();
						int i = 1;
						for (DistributeStaff d : result) {
							ticks.push(Tick.of(i,d.getUserLogin()));
							i++;
						};
						yOpt.setTicks(ticks);
						plotOptions.addYAxisOptions(yOpt);
						// axis X
						AxisOptions xOpt =  AxisOptions.create()
								.setLabel("Эффективность, %")
								.setMaximum(105)
								.setShow(true);
						plotOptions.addXAxisOptions(xOpt);
						
						PlotModel model = new PlotModel();
				    	Series graph = Series.of("");
				        SeriesHandler handler = model.addSeries(graph);
				        i = 1;
					    for (DistributeStaff p : result) {
					        handler.add(DataPoint.of(100*p.getDouble(),i));
					        i++;
						}
					    // draw
				        SimplePlot plot = new SimplePlot(model,plotOptions);
				        plot.setHeight(590);
				        plot.setWidth(769);
						
				        setWidget(PLOT_COORD_ROW, PLOT_COORD_COLUMN, plot);
				        getFlexCellFormatter().setHorizontalAlignment(PLOT_COORD_ROW, PLOT_COORD_COLUMN, HasHorizontalAlignment.ALIGN_CENTER);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						Ipplan.showError(caught);
					}
				});
				
			}
			break;
			case REPORT_DISTR_COST: {

				ChartOptions options = new ChartOptions();
				options.all = cbAll.getValue();
				options.excludeSelf = cbExcludeSelf.getValue();
				
				getService().distributeCosts(dStart, dFinish, options, new AsyncCallback<List<DistributeCost>>() {
					
					@Override
					public void onSuccess(List<DistributeCost> result) {
						PlotOptions plotOptions = PlotOptions.create();
						PieSeriesOptions opt = PieSeriesOptions.create()
								.setRadius(0.9)
								//.setStartAngle(0.6)
								//.setOffset(Offset.of(30, 30))
								.setTilt(0.5)
								.setStroke(Stroke.create().setWidth(2))
								.setLabel(com.googlecode.gflot.client.options.PieSeriesOptions.Label.create()
										.setFormatter(new Formatter() {
											@Override
											public String format(String label, Series series) {
												int data = (int)series.getData().getY(0);
												return  NumberFormat.getFormat("##0.0").format(series.getPercent())+"% ("+
														NumberFormat.getFormat("#,##0.00").format(data)+")";
											}
										})
										.setShow(true)
										)
								.setShow(true); 
						plotOptions.setGlobalSeriesOptions(GlobalSeriesOptions.create().setPieSeriesOptions(opt));
					    // готовим серии
						PlotModel model = new PlotModel();
					    for (DistributeCost data : result) {
					    	Series graph = Series.of(data.getCostsName());
					        SeriesHandler handler = model.addSeries(graph);
					        handler.add(PieDataPoint.of(data.getDoubleValue()/100));
						}
				        SimplePlot plot = new SimplePlot(model,plotOptions);
				        plot.setHeight(590);
				        plot.setWidth(769);
				        
				        setWidget(PLOT_COORD_ROW, PLOT_COORD_COLUMN, plot);
				        getFlexCellFormatter().setHorizontalAlignment(PLOT_COORD_ROW, PLOT_COORD_COLUMN, HasHorizontalAlignment.ALIGN_CENTER);
						
					}
					
					@Override
					public void onFailure(Throwable caught) {
						Ipplan.showError(caught);
					}
				});
				
			}
			break;

			case REPORT_EXEC_DYN: {
				ArrayList<Integer> statuses = new ArrayList<Integer>();
				for (ToggleButton toggle : toggles.keySet()) {
					if(toggle.isDown()) 
						statuses.add(toggles.get(toggle).statusId);
				}
				if(statuses.size()==0) {
					showError(this, 2, "Необходимо выбрать статус");
					return;
				}

				ChartOptions options = new ChartOptions();
				options.all = cbAll.getValue();
				options.excludeSelf = cbExcludeSelf.getValue();
				options.sales = false;
				
				getService().dynamicMonthData(dStart, dFinish, statuses.toArray(new Integer[]{}), options, new AsyncCallback<List<DynamicMonthSeries>>() {
					
					@Override
					public void onSuccess(final List<DynamicMonthSeries> result) {
						PlotOptions plotOptions = PlotOptions.create();
						
						double commonWidth = 0.7;
						double barWidth = commonWidth*1.0/result.size();
						
						BarSeriesOptions barOpt = BarSeriesOptions.create()
								.setShow(true)
								.setLineWidth(1)
								.setBarWidth(barWidth-0.05)
								.setFill(0.7)
								.setAlignment(BarAlignment.CENTER);
						GlobalSeriesOptions options = GlobalSeriesOptions.create().setBarsSeriesOptions(barOpt);
						plotOptions.setGlobalSeriesOptions(options);
						plotOptions.setLegendOptions(LegendOptions.create()
								//.setBackgroundColor("#FFFFF0")
								.setBackgroundOpacity(0)
								.setPosition(LegendPosition.NORTH_WEST)
								.setShow(true));
						// нужно взять кол-во точек, берем у первой серии
						final DynamicMonthSeries firstseria = result.size()>0?result.get(0):null;
						int pointcount = 0;
						if(result.size()>0) 
							pointcount = firstseria.data.size();
						// axis X
						if(firstseria!=null) {
							AxisOptions xOpt = AxisOptions.create().setTicks(pointcount);
							JsArray<Tick> ticks = JavaScriptObject.createArray().cast();
							for (DynamicMonthData d : firstseria.data) {
			            		String s = MONTH_NAMES[d.getMonth()-1];
			            		if(d.getMonth()==1) s+=",\n"+d.getYear()+" г.";
								ticks.push(Tick.of(d.getYear()*12+d.getMonth(),s));
							}
							xOpt.setTicks(ticks);
							plotOptions.addXAxisOptions(xOpt);
						}
						// axis Y
						AxisOptions yOpt =  AxisOptions.create()
								.setMinTickSize(1)
								.setLabel("Кол-во сделок");
						plotOptions.addYAxisOptions(yOpt);
						
					    // готовим серии
						PlotModel model = new PlotModel();
						double offs = (result.size()-1)*barWidth/2.0;
					    for (DynamicMonthSeries seria : result) {
					    	StatusWrapper sw = StatusWrapper.getAllStatuses().get(seria.statusId);
					    	Series graph = Series.of(sw.statusName)
					    			.setColor(StatusWrapper.getBackgroundColor(seria.statusId));
					    	graph.put("borderColor", "#939393");
					        SeriesHandler handler = model.addSeries(graph);
					        for (DynamicMonthData d : seria.data) {
						        handler.add(DataPoint.of(d.getYear()*12+d.getMonth()+offs, d.getCount()));
							}
					        offs-=barWidth;
						}
					    // draw
				        SimplePlot plot = new SimplePlot(model,plotOptions);
				        plot.setHeight(590);
				        plot.setWidth(769);
				        
				        setWidget(PLOT_COORD_ROW, PLOT_COORD_COLUMN, plot);
				        getFlexCellFormatter().setHorizontalAlignment(PLOT_COORD_ROW, PLOT_COORD_COLUMN, HasHorizontalAlignment.ALIGN_CENTER);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						Ipplan.showError(caught);
					}
				});
				
			};	
			break;

			case REPORT_FINANCE_DYN: {

				ChartOptions options = new ChartOptions();
				options.all = cbAll.getValue();
				options.excludeSelf = cbExcludeSelf.getValue();
				options.sales = false;
				options.onlyHead = true;
				
				getService().dynamicMonthData(dStart, dFinish, new Integer[]{StatusWrapper.CLOSE_OK}, options, new AsyncCallback<List<DynamicMonthSeries>>() {
					
					@Override
					public void onSuccess(final List<DynamicMonthSeries> result) {
						PlotOptions plotOptions = PlotOptions.create();
						
						BarSeriesOptions barOpt = BarSeriesOptions.create()
								.setShow(true)
								.setLineWidth(1)
								.setBarWidth(0.7)
								.setFill(0.7)
								.setAlignment(BarAlignment.CENTER);
						GlobalSeriesOptions options = GlobalSeriesOptions.create().setBarsSeriesOptions(barOpt);
						plotOptions.setGlobalSeriesOptions(options);
						plotOptions.setLegendOptions(LegendOptions.create()
								.setBackgroundOpacity(0)
								.setPosition(LegendPosition.NORTH_WEST)
								.setShow(true));
						// нужно взять кол-во точек, берем у первой серии
						final DynamicMonthSeries firstseria = result.size()>0?result.get(0):null;
						int pointcount = 0;
						if(result.size()>0) 
							pointcount = firstseria.data.size();
						// axis X
						if(firstseria!=null) {
							AxisOptions xOpt = AxisOptions.create().setTicks(pointcount);
							JsArray<Tick> ticks = JavaScriptObject.createArray().cast();
							for (DynamicMonthData d : firstseria.data) {
			            		String s = MONTH_NAMES[d.getMonth()-1];
			            		if(d.getMonth()==1) s+=",\n"+d.getYear()+" г.";
								ticks.push(Tick.of(d.getYear()*12+d.getMonth(),s));
							}
							xOpt.setTicks(ticks);
							plotOptions.addXAxisOptions(xOpt);
						}
						// axis Y
						AxisOptions yOpt =  AxisOptions.create()
								.setMinTickSize(1000)
								.setLabel("Сумма");
						plotOptions.addYAxisOptions(yOpt);
						
					    // готовим серии
						PlotModel model = new PlotModel();
						
				    	Series graphCosts = Series.of("Расходы")
				    			.setStack(true)
				    			//.setStack(1)
				    			.setColor("#004276");
				        SeriesHandler handlerCosts = model.addSeries(graphCosts);
				    	Series graphTax = Series.of("Налоги")
				    			.setStack(true)
				    			//.setStack(1)
				    			.setColor("#AA0000");
				        SeriesHandler handlerTax = model.addSeries(graphTax);
				    	Series graphFine = Series.of("Штрафы, пени")
				    			.setStack(true)
				    			//.setStack(1)
				    			.setColor("#FF0000");
				        SeriesHandler handlerFine = model.addSeries(graphFine);
				    	Series graphProfit = Series.of("Прибыль")
				    			.setStack(true)
				    			//.setStack(0)
				    			.setColor("#3CB371");
				        SeriesHandler handlerProfit = model.addSeries(graphProfit);
						
					    for (DynamicMonthSeries seria : result) {
					    	//graph.put("borderColor", "#939393");
					        for (DynamicMonthData d : seria.data) {
					        	handlerCosts.add(DataPoint.of(d.getYear()*12+d.getMonth(), d.getCosts()*1.0/100));
					        	handlerTax.add(DataPoint.of(d.getYear()*12+d.getMonth(), d.getTax()*1.0/100));
					        	handlerFine.add(DataPoint.of(d.getYear()*12+d.getMonth(), d.getFine()*1.0/100));
					        	handlerProfit.add(DataPoint.of(d.getYear()*12+d.getMonth(), (d.getRevenue()-d.getCosts()-d.getFine()-d.getTax())*1.0/100));
							}
						}
					    // draw
				        SimplePlot plot = new SimplePlot(model,plotOptions);
				        plot.setHeight(590);
				        plot.setWidth(769);
				        
				        setWidget(PLOT_COORD_ROW, PLOT_COORD_COLUMN, plot);
				        getFlexCellFormatter().setHorizontalAlignment(PLOT_COORD_ROW, PLOT_COORD_COLUMN, HasHorizontalAlignment.ALIGN_CENTER);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						Ipplan.showError(caught);
					}
				});
				
			};	
			break;
			
		default:
			break;
		}
		
	}

	protected int getCountMonth() {
		return 12;
	}
}

package reporting;

import analytics.UsageStatsService;
import audit.AuditTrailLogger;
import cache.ReportCache;
import config.ApplicationSettings;
import data.CustomerRepository;
import email.EmailNotificationService;
import export.PDFExportService;
import export.CSVExportService;
import format.DateFormatter;
import security.AccessControlService;

public class ReportOrchestrator {

    private final UsageStatsService usageStatsService;
    private final AuditTrailLogger auditTrailLogger;
    private final ReportCache reportCache;
    private final ApplicationSettings settings;
    private final CustomerRepository customerRepository;
    private final EmailNotificationService emailService;
    private final PDFExportService pdfExportService;
    private final CSVExportService csvExportService;
    private final DateFormatter dateFormatter;
    private final AccessControlService accessControl;

    public ReportOrchestrator(
            UsageStatsService usageStatsService,
            AuditTrailLogger auditTrailLogger,
            ReportCache reportCache,
            ApplicationSettings settings,
            CustomerRepository customerRepository,
            EmailNotificationService emailService,
            PDFExportService pdfExportService,
            CSVExportService csvExportService,
            DateFormatter dateFormatter,
            AccessControlService accessControl
    ) {
        this.usageStatsService = usageStatsService;
        this.auditTrailLogger = auditTrailLogger;
        this.reportCache = reportCache;
        this.settings = settings;
        this.customerRepository = customerRepository;
        this.emailService = emailService;
        this.pdfExportService = pdfExportService;
        this.csvExportService = csvExportService;
        this.dateFormatter = dateFormatter;
        this.accessControl = accessControl;
    }

    public void generateAndDistributeReport(String userId, String reportType) {
        if (!accessControl.hasPermission(userId, "GENERATE_REPORT")) {
            throw new SecurityException("User lacks permission.");
        }

        auditTrailLogger.log(userId + " requested report generation.");

        var customers = customerRepository.findAllActiveCustomers();
        var stats = usageStatsService.calculateStats(customers);

        String formattedDate = dateFormatter.formatNow();
        String reportContent = "Report generated on " + formattedDate + "\n" + stats;

        reportCache.storeReport(userId, reportType, reportContent);

        if ("PDF".equalsIgnoreCase(reportType)) {
            byte[] pdf = pdfExportService.export(reportContent);
            emailService.sendAttachment(userId, "Your PDF Report", pdf);
        } else if ("CSV".equalsIgnoreCase(reportType)) {
            byte[] csv = csvExportService.export(reportContent);
            emailService.sendAttachment(userId, "Your CSV Report", csv);
        }

        auditTrailLogger.log("Report successfully sent to " + userId);
    }
}
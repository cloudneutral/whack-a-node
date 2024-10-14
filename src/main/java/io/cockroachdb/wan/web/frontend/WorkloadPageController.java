package io.cockroachdb.wan.web.frontend;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import io.cockroachdb.wan.web.api.WorkloadController;
import io.cockroachdb.wan.web.api.model.WorkloadForm;
import io.cockroachdb.wan.web.api.model.WorkloadType;
import io.cockroachdb.wan.workload.WorkloadManager;

@Controller
@RequestMapping("/workload")
public class WorkloadPageController {
    @Autowired
    private WorkloadController workloadController;

    @Autowired
    private WorkloadManager workloadManager;

    @Value("${application.adminUrl}")
    private String adminUrl;

    @GetMapping
    public Callable<String> workloadPage(Model model) {
        WorkloadForm form = new WorkloadForm();
        form.setDuration("00:15");
        form.setWorkloadType(WorkloadType.random_wait);

        model.addAttribute("adminUrl", this.adminUrl);
        model.addAttribute("form", form);
        model.addAttribute("workloads", workloadController.listWorkloads().getBody());
        model.addAttribute("aggregatedMetrics", workloadManager.getAggregatedMetrics());

        return () -> "workload";
    }

    @PostMapping
    public Callable<String> submitWorkload(@ModelAttribute WorkloadForm form, Model model) {
        workloadController.startWorkload(form);
        model.addAttribute("form", form);
        return () -> "redirect:workload";
    }

    @PostMapping(value = "/cancelAll")
    public RedirectView cancelAllWorkloads() {
        workloadManager.cancelAll();
        return new RedirectView("/workload");
    }

    @PostMapping(value = "/deleteAll")
    public RedirectView deleteAllWorkloads() {
        workloadManager.deleteAll();
        return new RedirectView("/workload");
    }

    @PostMapping(value = "/cancel/{id}")
    public RedirectView cancelWorkload(@PathVariable("id") Integer id) {
        workloadController.cancelWorkload(id);
        return new RedirectView("/workload");
    }

    @PostMapping(value = "/delete/{id}")
    public RedirectView deleteWorkload(@PathVariable("id") Integer id) {
        workloadController.deleteWorkload(id);
        return new RedirectView("/workload");
    }
}

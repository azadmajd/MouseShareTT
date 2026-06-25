package de.ugdata.mousesharett.controller;

import de.ugdata.mousesharett.model.ScreenPos;
import de.ugdata.mousesharett.service.WorkerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api")
public class LayoutController {

    @Autowired
    private WorkerRegistry workerRegistry;

    @GetMapping("/workers")
    public Collection<WorkerRegistry.WorkerInfo> getWorkers() {
        return workerRegistry.getActiveWorkers();
    }

    @PostMapping("/layout")
    public void saveLayout(@RequestBody List<ScreenPos> layout) {
        System.out.println("Received layout with " + layout.size() + " screens");
        for (ScreenPos pos : layout) {
            System.out.println("Screen: " + pos.getId() + " at " + pos.getX() + "," + pos.getY());
        }
    }
}

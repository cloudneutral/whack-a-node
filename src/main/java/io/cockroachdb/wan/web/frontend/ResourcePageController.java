package io.cockroachdb.wan.web.frontend;

import java.util.concurrent.Callable;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequestMapping("/")
public class ResourcePageController {
    @GetMapping("/hal-explorer")
    public Callable<String> halExplorer() {
        String rootUri = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .pathSegment("api")
                .buildAndExpand()
                .toUriString();
        return () -> "redirect:browser/index.html#theme=Darkly&uri=" + rootUri;
    }

    @GetMapping("/rels/{name}")
    public String relPage(@PathVariable("name") String name) {
        return "rels/" + name;
    }
}

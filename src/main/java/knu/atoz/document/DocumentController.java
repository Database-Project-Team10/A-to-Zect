package knu.atoz.document;

import knu.atoz.document.dto.DocumentRequestDto;
import knu.atoz.document.exception.DocumentException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/documents")
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public String listDocuments(@RequestParam Long projectId, Model model) {
        try {
            List<Document> documents = documentService.getDocumentsByProject(projectId);
            model.addAttribute("documents", documents);
            model.addAttribute("projectId", projectId);

            if (model.asMap().containsKey("message")) {
                model.addAttribute("message", model.asMap().get("message"));
            }
            if (model.asMap().containsKey("error")) {
                model.addAttribute("error", model.asMap().get("error"));
            }

            return "document/list";
        } catch (DocumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("projectId", projectId);
            return "document/list";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(@RequestParam Long projectId, Model model) {
        model.addAttribute("documentRequestDto", new DocumentRequestDto("", ""));
        model.addAttribute("isNew", true);
        model.addAttribute("projectId", projectId);

        model.addAttribute("actionUrl", "/documents?projectId=" + projectId);


        if (model.asMap().containsKey("error")) {

            model.addAttribute("error", model.asMap().get("error"));

        }
        return "document/form";
    }

    @PostMapping
    public String createDocument(@RequestParam Long projectId,
                                 @ModelAttribute DocumentRequestDto requestDto,
                                 RedirectAttributes redirectAttributes) {
        try {
            documentService.createDocument(projectId, requestDto);
            redirectAttributes.addFlashAttribute("message", "문서가 성공적으로 작성되었습니다.");
            return "redirect:/documents?projectId=" + projectId;
        } catch (DocumentException e) {
            redirectAttributes.addFlashAttribute("error", "[!] " + e.getMessage());
            return "redirect:/documents/new?projectId=" + projectId;
        }
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable Long id, @RequestParam Long projectId, Model model) {
        try {
            Document targetDocument = documentService.getDocument(id);
            DocumentRequestDto dto = new DocumentRequestDto(targetDocument.getTitle(), targetDocument.getLocation());

            model.addAttribute("document", targetDocument);
            model.addAttribute("documentRequestDto", dto);
            model.addAttribute("isNew", false);
            model.addAttribute("projectId", projectId);

            model.addAttribute("actionUrl", "/documents/" + id + "?projectId=" + projectId);

            if (model.asMap().containsKey("error")) {
                model.addAttribute("error", model.asMap().get("error"));
            }

            return "document/form";
        } catch (DocumentException e) {
            return "redirect:/documents?projectId=" + projectId;
        }
    }

    @PutMapping("/{id}")
    public String updateDocument(@PathVariable Long id, @RequestParam Long projectId,
                                 @ModelAttribute DocumentRequestDto requestDto,
                                 RedirectAttributes redirectAttributes) {
        try {
            documentService.updateDocument(id, projectId, requestDto);
            redirectAttributes.addFlashAttribute("message", "문서가 성공적으로 수정되었습니다.");
            return "redirect:/documents?projectId=" + projectId;
        } catch (DocumentException e) {
            redirectAttributes.addFlashAttribute("error", "[!] " + e.getMessage());
            return "redirect:/documents/" + id + "/edit?projectId=" + projectId;
        }
    }

    @DeleteMapping("/{id}")
    public String deleteDocument(@PathVariable Long id, @RequestParam Long projectId,
                                 RedirectAttributes redirectAttributes) {
        try {
            documentService.deleteDocument(id, projectId);
            redirectAttributes.addFlashAttribute("message", "문서가 성공적으로 삭제되었습니다.");
        } catch (DocumentException e) {
            redirectAttributes.addFlashAttribute("error", "[!] " + e.getMessage());
        }
        return "redirect:/documents?projectId=" + projectId;
    }
}
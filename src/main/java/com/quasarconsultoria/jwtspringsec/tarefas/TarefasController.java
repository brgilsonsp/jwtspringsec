package com.quasarconsultoria.jwtspringsec.tarefas;

import com.quasarconsultoria.jwtspringsec.comum.AcessoNegadoException;
import com.quasarconsultoria.jwtspringsec.model.Tarefa;
import com.quasarconsultoria.jwtspringsec.model.TarefasRepository;
import com.quasarconsultoria.jwtspringsec.model.Usuario;
import com.quasarconsultoria.jwtspringsec.model.UsuariosRepository;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tarefas")
class TarefasController {

    private TarefasRepository tarefasRepository;
    private UsuariosRepository usuariosRepository;

    TarefasController(TarefasRepository tarefasRepository,
                      UsuariosRepository usuariosRepository) {
        this.tarefasRepository = tarefasRepository;
        this.usuariosRepository = usuariosRepository;
    }

    @GetMapping
    List<TarefaDTO> buscarTodas(Principal principal) {
        String loginDoUsuario = principal.getName();
        Usuario usuario = this.usuariosRepository.findByLogin(loginDoUsuario).get();
        return this.tarefasRepository
                .findByUsuario(usuario).stream()
                .map(TarefaDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    TarefaDetalhadaDTO buscarPorId(@PathVariable("id") Integer id, Principal principal) {
        String loginDoUsuario = principal.getName();
        Usuario usuario = this.usuariosRepository.findByLogin(loginDoUsuario).get();
        Tarefa tarefa = this.tarefasRepository.findById(id).get();
        if (!tarefa.getUsuario().getId().equals(usuario.getId())) {
            throw new AcessoNegadoException();
        }
        return new TarefaDetalhadaDTO(tarefa);
    }

    @PostMapping
    void cadastrar(@RequestBody NovaTarefaDTO tarefa, Principal principal) {
        String loginDoUsuario = principal.getName();
        Usuario usuario = this.usuariosRepository.findByLogin(loginDoUsuario).get();
        Tarefa entidade = new Tarefa();
        entidade.setUsuario(usuario);
        entidade.setDescricao(tarefa.getDescricao());
        entidade.setCriadaEm(LocalDateTime.now());
        this.tarefasRepository.save(entidade);
    }

    private Usuario getUsuarioLogado(HttpSession session) {
        Integer idUsuarioLogado = (Integer)session.getAttribute("idUsuarioLogado");
        return this.usuariosRepository.findById(idUsuarioLogado).get();
    }

}

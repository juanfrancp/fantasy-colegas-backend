package com.fantasycolegas.fantasy_colegas_backend.service;

import com.fantasycolegas.fantasy_colegas_backend.dto.request.ReplacePlayerDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.RosterCreateDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.request.RosterPlayerDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.PlayerResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.dto.response.RosterPlayerResponseDto;
import com.fantasycolegas.fantasy_colegas_backend.model.League;
import com.fantasycolegas.fantasy_colegas_backend.model.Player;
import com.fantasycolegas.fantasy_colegas_backend.model.RosterPlayer;
import com.fantasycolegas.fantasy_colegas_backend.model.User;
import com.fantasycolegas.fantasy_colegas_backend.model.enums.PlayerTeamRole;
import com.fantasycolegas.fantasy_colegas_backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Juan Francisco Carceles
 * @version 1.0
 * @since 01/08/2025
 * <p>
 * Servicio para la gestión de equipos (rosters) de los usuarios en una liga.
 * <p>
 * Contiene la lógica de negocio para crear, ver y modificar los equipos
 * de los participantes, con validaciones de tamaño y roles de los jugadores.
 * </p>
 */
@Service
public class RosterService {

    private final RosterPlayerRepository rosterPlayerRepository;
    private final LeagueService leagueService;
    private final LeagueRepository leagueRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;
    private final PlayerMatchStatsRepository playerMatchStatsRepository;

    /**
     * Constructor del servicio que inyecta las dependencias de los repositorios y otros servicios.
     */
    public RosterService(RosterPlayerRepository rosterPlayerRepository, LeagueService leagueService, LeagueRepository leagueRepository, PlayerRepository playerRepository, UserRepository userRepository, PlayerMatchStatsRepository playerMatchStatsRepository) {
        this.rosterPlayerRepository = rosterPlayerRepository;
        this.leagueService = leagueService;
        this.leagueRepository = leagueRepository;
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
        this.playerMatchStatsRepository = playerMatchStatsRepository;
    }

    /**
     * Crea o reemplaza el equipo de un usuario en una liga.
     * <p>
     * Se realizan varias validaciones, como la pertenencia del usuario a la liga,
     * el tamaño del equipo, el número de porteros y la existencia de los jugadores.
     * </p>
     *
     * @param leagueId        El ID de la liga.
     * @param rosterCreateDto DTO con la lista de jugadores y sus roles.
     * @param userId          El ID del usuario que crea el equipo.
     * @return Un mensaje de confirmación del éxito.
     */
    @Transactional
    public String createRoster(Long leagueId, RosterCreateDto rosterCreateDto, Long userId) {
        if (!leagueService.isUserParticipant(leagueId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo los participantes de la liga pueden crear un equipo.");
        }

        League league = leagueRepository.findById(leagueId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada."));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        int requestedSize = rosterCreateDto.getPlayers().size();
        if (requestedSize != league.getTeamSize()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tamaño del equipo debe ser " + league.getTeamSize() + ", pero se han enviado " + requestedSize + " jugadores.");
        }

        long porteroCount = rosterCreateDto.getPlayers().stream().filter(p -> p.getRole() == PlayerTeamRole.PORTERO).count();
        if (porteroCount != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El equipo debe tener exactamente un portero.");
        }

        List<Long> playerIds = rosterCreateDto.getPlayers().stream().map(RosterPlayerDto::getPlayerId).collect(Collectors.toList());

        List<com.fantasycolegas.fantasy_colegas_backend.model.Player> existingPlayers = playerRepository.findAllById(playerIds);
        if (existingPlayers.size() != requestedSize) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uno o más jugadores no se encontraron.");
        }
        if (existingPlayers.stream().anyMatch(p -> !p.getLeague().getId().equals(leagueId))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uno o más jugadores no pertenecen a esta liga.");
        }

        rosterPlayerRepository.deleteByUserIdAndLeagueId(userId, leagueId);

        List<RosterPlayer> rosterPlayers = rosterCreateDto.getPlayers().stream().map(rosterPlayerDto -> {
            RosterPlayer rosterPlayer = new RosterPlayer();
            rosterPlayer.setUser(user);
            rosterPlayer.setLeague(league);

            com.fantasycolegas.fantasy_colegas_backend.model.Player player = existingPlayers.stream().filter(p -> p.getId().equals(rosterPlayerDto.getPlayerId())).findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Jugador no encontrado en la lista de la liga."));

            rosterPlayer.setPlayer(player);
            rosterPlayer.setRole(rosterPlayerDto.getRole());
            return rosterPlayer;
        }).collect(Collectors.toList());

        rosterPlayerRepository.saveAll(rosterPlayers);

        return "Equipo de la jornada guardado con éxito.";
    }

    /**
     * Obtiene el equipo (roster) de un usuario en una liga.
     * <p>
     * Solo los participantes de la liga pueden ver su propio equipo.
     * </p>
     *
     * @param leagueId El ID de la liga.
     * @param userId   El ID del usuario.
     * @return Una lista de {@link RosterPlayerResponseDto} con los detalles de los jugadores del equipo.
     */
    public List<RosterPlayerResponseDto> getUserRoster(Long leagueId, Long userId) {
        if (!leagueService.isUserParticipant(leagueId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo los participantes de la liga pueden ver su equipo.");
        }

        List<RosterPlayer> rosterPlayers = rosterPlayerRepository.findByUserIdAndLeagueId(userId, leagueId);

        return rosterPlayers.stream().map(rosterPlayer -> new RosterPlayerResponseDto(rosterPlayer.getPlayer().getId(), rosterPlayer.getPlayer().getName(), rosterPlayer.getRole(), rosterPlayer.getPlayer().getImage(), rosterPlayer.getPlayer().getTotalPoints())).collect(Collectors.toList());
    }

    /**
     * Elimina un jugador de un equipo y lo reemplaza por un jugador 'placeholder'.
     *
     * @param leagueId         El ID de la liga.
     * @param userId           El ID del usuario.
     * @param playerIdToRemove El ID del jugador a eliminar.
     * @return Un mensaje de confirmación.
     */
    @Transactional
    public String removePlayerFromRoster(Long leagueId, Long userId, Long playerIdToRemove) {
        if (!leagueService.isUserParticipant(leagueId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo los participantes de la liga pueden modificar su equipo.");
        }

        List<RosterPlayer> roster = rosterPlayerRepository.findByUserIdAndLeagueId(userId, leagueId);
        if (roster.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no tiene un equipo en esta liga.");
        }

        Optional<RosterPlayer> playerToRemoveOpt = roster.stream().filter(rp -> rp.getPlayer().getId().equals(playerIdToRemove)).findFirst();

        if (playerToRemoveOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El jugador a eliminar no se encuentra en tu equipo.");
        }

        RosterPlayer rosterPlayerToRemove = playerToRemoveOpt.get();
        Player playerToRemove = rosterPlayerToRemove.getPlayer();

        if (playerToRemove.isPlaceholder()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes eliminar al jugador vacío.");
        }

        Player placeholderPlayer = playerRepository.findByIsPlaceholderTrue().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El jugador vacío no se encuentra en la base de datos. Contacta con el administrador."));

        rosterPlayerToRemove.setPlayer(placeholderPlayer);
        if (rosterPlayerToRemove.getRole() == PlayerTeamRole.PORTERO) {
            rosterPlayerToRemove.setRole(PlayerTeamRole.PORTERO);
        } else {
            rosterPlayerToRemove.setRole(PlayerTeamRole.CAMPO);
        }

        rosterPlayerRepository.save(rosterPlayerToRemove);

        return "Jugador eliminado y reemplazado con éxito.";
    }

    /**
     * Añade un jugador a un equipo.
     * Si encuentra un hueco (placeholder), lo ocupa.
     * Si no hay hueco pero el equipo no está lleno (faltan filas en BBDD), crea la fila nueva.
     */
    @Transactional
    public String addPlayerToRoster(Long leagueId, Long userId, Long playerIdToAdd, PlayerTeamRole position) {
        // 1. Validaciones básicas
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Liga no encontrada."));

        Player playerToAdd = playerRepository.findById(playerIdToAdd)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El jugador a añadir no existe."));

        // 2. Verificar duplicados
        boolean playerAlreadyInRoster = rosterPlayerRepository.existsByUserIdAndLeagueIdAndPlayerId(userId, leagueId, playerIdToAdd);
        if (playerAlreadyInRoster) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El jugador ya se encuentra en tu equipo.");
        }

        // 3. Obtener el equipo ACTUAL
        List<RosterPlayer> currentRoster = rosterPlayerRepository.findByUserIdAndLeagueId(userId, leagueId);

        // 4. ESTRATEGIA DE BÚSQUEDA DE HUECO
        // Paso A: Buscar hueco perfecto (mismo rol)
        Optional<RosterPlayer> bestSlot = currentRoster.stream()
                .filter(rp -> rp.getPlayer().isPlaceholder() && rp.getRole() == position)
                .findFirst();

        // Paso B: Si no hay perfecto, buscar CUALQUIER hueco (reciclaje)
        if (bestSlot.isEmpty()) {
            bestSlot = currentRoster.stream()
                    .filter(rp -> rp.getPlayer().isPlaceholder())
                    .findFirst();
        }

        if (bestSlot.isPresent()) {
            // ESCENARIO A: Encontramos un hueco (perfecto o reciclado) -> Lo ocupamos
            RosterPlayer slotToUpdate = bestSlot.get();
            slotToUpdate.setPlayer(playerToAdd);
            slotToUpdate.setRole(position); // IMPORTANTE: Forzamos el rol nuevo
            rosterPlayerRepository.save(slotToUpdate);
        } else {
            // ESCENARIO B: No hay huecos de ningún tipo -> Intentamos CREAR
            if (currentRoster.size() < league.getTeamSize()) {
                // Validar restricción de porteros
                if (position == PlayerTeamRole.PORTERO && currentRoster.stream().anyMatch(rp -> rp.getRole() == PlayerTeamRole.PORTERO)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya tienes un portero en el equipo.");
                }

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

                RosterPlayer newSlot = new RosterPlayer();
                newSlot.setUser(user);
                newSlot.setLeague(league);
                newSlot.setPlayer(playerToAdd);
                newSlot.setRole(position);
                rosterPlayerRepository.save(newSlot);
            } else {
                // ESCENARIO C: Equipo lleno y sin huecos
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tu equipo está lleno (" + currentRoster.size() + "/" + league.getTeamSize() + ") y no hay huecos libres.");
            }
        }

        return "Jugador " + playerToAdd.getName() + " añadido a tu equipo con éxito.";
    }

    public List<PlayerResponseDto> getAvailablePlayers(Long leagueId, Long userId) {
        List<Long> currentPlayerIds = rosterPlayerRepository.findByUserIdAndLeagueId(userId, leagueId)
                .stream()
                .map(rosterPlayer -> rosterPlayer.getPlayer().getId())
                .collect(Collectors.toList());

        List<Player> availablePlayers = playerRepository.findAvailablePlayers(leagueId, currentPlayerIds);

        // CORRECCIÓN AQUÍ
        return availablePlayers.stream()
                .map(player -> {
                    // Consultamos los puntos desglosados (Campo vs Portero)
                    Double fieldPoints = playerMatchStatsRepository.sumTotalFieldPointsByPlayer(player.getId());
                    Double gkPoints = playerMatchStatsRepository.sumTotalGoalkeeperPointsByPlayer(player.getId());

                    // Aseguramos que no sean nulos
                    if (fieldPoints == null) fieldPoints = 0.0;
                    if (gkPoints == null) gkPoints = 0.0;

                    int totalCalculated = (int) (fieldPoints + gkPoints);

                    // Llamamos al nuevo constructor de 6 argumentos
                    return new PlayerResponseDto(
                            player.getId(),
                            player.getName(),
                            player.getImage(),
                            totalCalculated, // Total
                            fieldPoints,     // Puntos Campo
                            gkPoints
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public String replacePlayerInRoster(Long leagueId, Long userId, ReplacePlayerDto replacePlayerDto) {
        RosterPlayer rosterEntryToUpdate = rosterPlayerRepository.findByUserIdAndLeagueId(userId, leagueId).stream()
                .filter(rp -> rp.getPlayer().getId().equals(replacePlayerDto.getPlayerToRemoveId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El jugador a eliminar no está en tu equipo."));

        String playerToRemoveName = rosterEntryToUpdate.getPlayer().getName();

        Player playerToAdd = playerRepository.findById(replacePlayerDto.getPlayerToAddId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El jugador a añadir no existe."));

        rosterEntryToUpdate.setPlayer(playerToAdd);
        rosterPlayerRepository.save(rosterEntryToUpdate);

        return "Jugador " + playerToRemoveName + " reemplazado por " + playerToAdd.getName() + " con éxito.";
    }
}
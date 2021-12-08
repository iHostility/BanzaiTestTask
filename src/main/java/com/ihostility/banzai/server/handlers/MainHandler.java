package com.ihostility.banzai.server.handlers;

import com.ihostility.banzai.server.entity.Player;
import com.ihostility.banzai.server.enums.Action;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MainHandler extends SimpleChannelInboundHandler<String> {
    private static final Map<Channel, Player> playersAwaiting = new HashMap<>();
    private static final Map<Channel, Player> playersReady = new HashMap<>();
    private static final List<Player> game = new ArrayList<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        try {
            log.info("Новый игрок подключился! " + ctx);
            playersAwaiting.put(ctx.channel(), new Player());
            ctx.channel().writeAndFlush("Представься, пожалуйста.\n");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String request) {
        try {
            if (game.size() > 1) {
                log.info("Starting game");
                List<Player> newGame = new ArrayList<>(game);
                game.clear();
                makeGame(newGame);
                return;
            }

            if (playersReady.containsKey(ctx.channel()) && playersReady.get(ctx.channel()).getAction() == null){
                makeAction(ctx.channel(), request);
                return;
            }

            if (playersAwaiting.containsKey(ctx.channel()) && playersAwaiting.get(ctx.channel()).getNickname() == null) {
                playersAwaiting.get(ctx.channel()).setNickname(request);
                playersAwaiting.get(ctx.channel()).setChannel(ctx.channel());
                ctx.writeAndFlush("Подбираем соперника!\n");
                playersReady.put(ctx.channel(), playersAwaiting.get(ctx.channel()));
                playersAwaiting.remove(ctx.channel());

                if (playersReady.size() > 1) {
                    log.info("player ready size > 1");
                    playersReady.forEach((k, v) -> {
                        k.writeAndFlush("Соперник найден!\n");
                        k.writeAndFlush("Выберите действие: камень, ножницы, бумага!\n");
                    });
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("error in " + ctx);
        log.error(cause.getMessage());

    }

    private void makeAction(Channel ch, String s) {
        String input = s.toUpperCase();
        char firstLetter = input.charAt(0);
        if (firstLetter == 'К' || firstLetter == 'Н' || firstLetter == 'Б') {
            switch (firstLetter) {
                case 'К':
                    playersReady.get(ch).setAction(Action.ROCK);
                    game.add(playersReady.get(ch));
                    playersReady.remove(ch);
                    ch.writeAndFlush("Действие выбрано!\n");
                    if (game.size() > 1) {
                        List<Player> newGame = new ArrayList<>(game);
                        game.clear();
                        makeGame(newGame);
                    } return;
                case 'Н':
                    playersReady.get(ch).setAction(Action.SCISSORS);
                    game.add(playersReady.get(ch));
                    playersReady.remove(ch);
                    ch.writeAndFlush("Действие выбрано!\n");
                    if (game.size() > 1) {
                        List<Player> newGame = new ArrayList<>(game);
                        game.clear();
                        makeGame(newGame);
                    } return;
                case 'Б':
                    playersReady.get(ch).setAction(Action.PAPER);
                    game.add(playersReady.get(ch));
                    playersReady.remove(ch);
                    ch.writeAndFlush("Действие выбрано!\n");
                    if (game.size() > 1) {
                        List<Player> newGame = new ArrayList<>(game);
                        game.clear();
                        makeGame(newGame);
                    }
            }
        }
    }

    private void makeGame(List<Player> players) {
        log.info(players.toString());
        Action player1 = players.get(0).getAction();
        Action player2 = players.get(1).getAction();
        int compareActions = player1.compareAction(player2);
        switch (compareActions) {
            case 0:
                players.forEach(player -> {
                    player.getChannel().writeAndFlush(players.get(0).getNickname() + " выбрасывает " + players.get(0).getAction().getTitle() + "\n");
                    player.getChannel().writeAndFlush(players.get(1).getNickname() + " выбрасывает " + players.get(1).getAction().getTitle() + "\n");
                    player.getChannel().writeAndFlush("Ничья! Переигрываем!\n");
                    player.getChannel().writeAndFlush("Выберите действие: камень, ножницы, бумага!\n");
                });
                playersReady.put(players.get(0).getChannel(), players.get(0));
                playersReady.put(players.get(1).getChannel(), players.get(1));
                playersReady.get(players.get(0).getChannel()).setAction(null);
                playersReady.get(players.get(1).getChannel()).setAction(null);

                log.info("Players ready: " + playersReady);

                players.clear();
                return;
            case -1:
                players.forEach(player -> {
                    player.getChannel().writeAndFlush(players.get(0).getNickname() + " выбрасывает " + players.get(0).getAction().getTitle() + "\n");
                    player.getChannel().writeAndFlush(players.get(1).getNickname() + " выбрасывает " + players.get(1).getAction().getTitle() + "\n");
                    player.getChannel().writeAndFlush(players.get(1).getNickname() + " победил!\n");
                });

                players.get(0).getChannel().disconnect();
                players.get(1).getChannel().disconnect();
                return;
            case 1:
                players.forEach(player -> {
                    player.getChannel().writeAndFlush(players.get(0).getNickname() + " выбрасывает " + players.get(0).getAction().getTitle() + "\n");
                    player.getChannel().writeAndFlush(players.get(1).getNickname() + " выбрасывает " + players.get(1).getAction().getTitle() + "\n");
                    player.getChannel().writeAndFlush(players.get(0).getNickname() + " победил!\n");
                });
                players.get(0).getChannel().disconnect();
                players.get(1).getChannel().disconnect();
        }
    }
}

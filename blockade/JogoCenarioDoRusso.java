package br.com.mvbos.lgj;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Random;

import br.com.mvbos.lgj.base.CenarioPadrao;
import br.com.mvbos.lgj.base.Elemento;
import br.com.mvbos.lgj.base.Texto;
import br.com.mvbos.lgj.base.Util;
import br.com.mvbos.lgj.rank.player;
import br.com.mvbos.lgj.rank.rank;

import javax.swing.*;

public class JogoCenarioDoRusso extends CenarioPadrao {

	enum Estado {
		JOGANDO, GANHOU, PERDEU
	}

	private static final int _LARG = 80;

	private int dx, dy;

	private boolean moveu;

	private Elemento fruta;

	private Elemento serpente;

	private Elemento[] rastros;

	private int blocoPorTela;

	private int temporizador = 0;

	private long timerInicio = 0;

	private long timerFinal = 0;

	private int cronometro = 0;

	private int contadorRastro = 5;

	private Texto texto = new Texto();

	private Random rand = new Random();

	private boolean flagRank = false;

	private boolean flagEndAudios = true;

	private boolean flagEatAudio = true;

	private Estado estado = Estado.JOGANDO;

	public JogoCenarioDoRusso(int largura, int altura) {
		super(largura, altura);
	}

	public void play(String nomeDoAudio){
		URL url = getClass().getResource(nomeDoAudio+".wav");
		AudioClip audio = Applet.newAudioClip(url);
		audio.play();
	}

	@Override
	public void carregar() {
		blocoPorTela = (largura / _LARG) * (altura / _LARG) - 1;

		rastros = new Elemento[blocoPorTela + contadorRastro];

		fruta = new Elemento(0, 0, _LARG, _LARG);
		fruta.setCor(Color.RED);

		serpente = new Elemento(0, 0, _LARG, _LARG);
		serpente.setAtivo(true);
		serpente.setCor(Color.YELLOW);

		Util.centraliza(serpente, largura, altura);

		// define direcao inicial
		dy = -1;

		serpente.setVel(Jogo.velocidade);

		for (int i = 0; i < rastros.length; i++) {
			rastros[i] = new Elemento(serpente.getPx(), serpente.getPy(), _LARG, _LARG);
			rastros[i].setCor(Color.GREEN);
			rastros[i].setAtivo(true);
		}

		timerInicio = System.currentTimeMillis();
	}

	@Override
	public void descarregar() {
		serpente = null;
		rastros = null;
		fruta = null;
	}

	@Override
	public void atualizar() {

		if (estado != Estado.JOGANDO) {
			return;
		}

		if (!moveu) {
			if (dy != 0) {
				if (Jogo.controleTecla[Jogo.Tecla.ESQUERDA.ordinal()]) {
					dx = -1;

				} else if (Jogo.controleTecla[Jogo.Tecla.DIREITA.ordinal()]) {
					dx = 1;
				}

				if (dx != 0) {
					dy = 0;
					moveu = true;
				}

			} else if (dx != 0) {
				if (Jogo.controleTecla[Jogo.Tecla.CIMA.ordinal()]) {
					dy = -1;
				} else if (Jogo.controleTecla[Jogo.Tecla.BAIXO.ordinal()]) {
					dy = 1;
				}

				if (dy != 0) {
					dx = 0;
					moveu = true;
				}
			}
		}

		if (temporizador >= 20) {
			temporizador = 0;
			moveu = false;

			int x = serpente.getPx();
			int y = serpente.getPy();

			// Somando mais um em _LARG para espacamento
			serpente.setPx(serpente.getPx() + (_LARG + 1) * dx);
			serpente.setPy(serpente.getPy() + (_LARG + 1) * dy);

			if (Util.saiu(serpente, largura, altura)) {
				serpente.setAtivo(false);
				estado = Estado.PERDEU;

			} else {

				for (int i = 0; i < contadorRastro; i++) {
					if (Util.colide(serpente, rastros[i])) {
						serpente.setAtivo(false);
						estado = Estado.PERDEU;
						break;
					}
				}
			}

			if (Util.colide(fruta, serpente)) {
				contadorRastro++;
				fruta.setAtivo(false);
				if (flagEatAudio) {
					play("eatFx");
					flagEatAudio = false;
				}

				if (blocoPorTela - contadorRastro == 0) {
					serpente.setAtivo(false);
					estado = Estado.GANHOU;
				}

			}

			for (int i = 0; i < contadorRastro; i++) {
				Elemento rastro = rastros[i];
				int tx = rastro.getPx();
				int ty = rastro.getPy();

				rastro.setPx(x);
				rastro.setPy(y);

				x = tx;
				y = ty;
			}

		} else
			temporizador += serpente.getVel();

		if (estado == Estado.JOGANDO && blocoPorTela - contadorRastro > 0) {
			while (!adicionaProximaFruta())
				;
		}

		//Rank

		boolean contineoLoop = true;

		if (estado == Estado.GANHOU || estado == Estado.PERDEU)
			flagRank = true;
		else
			flagRank = false;

		if (flagRank)
			do {
				try {
					rank rank = new rank(Jogo.nivel);

					if (rank.verifica(String.valueOf(rastros.length - contadorRastro), String.valueOf(cronometro))) {
						//Criando o painel para receber as informaçoes do rank

						JFrame frame = new JFrame("K19 - Java OO");
						frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

						JPanel panel = new JPanel();
						JLabel rotulo = new JLabel();

						rotulo.setText("Nome: ");
						panel.add(rotulo);
						JTextField textField = new JTextField(3);
						panel.add(textField);

						//configurando os butoes

						JButton button1 = new JButton("SIM");
						button1.addActionListener(
								new ActionListener() {
									public void actionPerformed( ActionEvent event ) {
										boolean contineoLoop1 = true;
										do{
											try{
												//Recebendo os dados

												if (textField.getText().length() == 0)
													textField.setText("***");
												if (textField.getText().length() > 3)
													textField.setText(textField.getText().substring(0, 3));
												player o = new player(textField.getText(), String.valueOf(rastros.length - contadorRastro), String.valueOf(cronometro));

												//Fechando as operacoes

												rank.atualizaRank(o, Jogo.nivel);
												contineoLoop1 = false;
												frame.dispose();
											}catch (Exception e) {
												System.out.println("Ocorreu um erro, " + e);
											}
										}while (contineoLoop1);
									}
								}
						);
						// botao de cancelar caso nao queira entrar no rank

						JButton button2 = new JButton("CANCEL");
						button2.addActionListener(
								new ActionListener() {
									public void actionPerformed( ActionEvent event ) {

										frame.dispose();
									}
								}
						);

						panel.add(button1);
						panel.add(button2);

						frame.setContentPane(panel);
						frame.pack();
						frame.setVisible(true);

					}
					contineoLoop = false;
					flagRank = false;
				} catch (Exception e) {
					System.out.println("Ocorreu um erro, " + e);
				}
			} while (contineoLoop);

	}

	private boolean adicionaProximaFruta() {

		// Adicionando frutas
		if (!fruta.isAtivo()) {
			int x = rand.nextInt(largura - _LARG);
			int y = rand.nextInt(altura - _LARG);

			fruta.setPx(x);
			fruta.setPy(y);

			fruta.setAtivo(true);

			if (Util.colide(fruta, serpente)) {
				fruta.setAtivo(false);

			} else {
				for (int i = 0; i < contadorRastro; i++) {
					if (Util.colide(fruta, rastros[i])) {
						fruta.setAtivo(false);
						break;
					}
				}
			}

		}

		return fruta.isAtivo();
	}

	@Override
	public void desenhar(Graphics2D g) {

		if (fruta.isAtivo()) {
			fruta.desenha(g);
		}

		for (int i = 0; i < contadorRastro; i++) {
			Elemento rastro = rastros[i];
			rastro.desenha(g);
		}

		serpente.desenha(g);

		timerFinal = System.currentTimeMillis();
		if (timerFinal - timerInicio >= 1000 && estado == Estado.JOGANDO) {
			timerInicio = System.currentTimeMillis();
			cronometro += 1;
		}
		texto.setCor(Color.RED);
		texto.desenha(g, "Tempo: " + String.valueOf(cronometro), 10, altura);

		texto.desenha(g, String.valueOf(rastros.length - contadorRastro), largura - 35, altura);

		if (estado != Estado.JOGANDO) {
			texto.setCor(Color.WHITE);

			if (estado == Estado.GANHOU) {
				texto.desenha(g, "Ganhou!", 180, 180);
				if (flagEndAudios) {
					play("winFx");
					flagEndAudios = false;
				}
			}
			else {
				texto.desenha(g, "Vixe!", 180, 180);
				if (flagEndAudios) {
					play("Game Over");
					flagEndAudios = false;
				}
			}

		} else if (Jogo.pausado)
			Jogo.textoPausa.desenha(g, "PAUSA", largura / 2 - Jogo.textoPausa.getFonte().getSize(), altura / 2);
	}

}

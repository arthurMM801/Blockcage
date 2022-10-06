package br.com.mvbos.lgj;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Random;
import java.util.Scanner;

import br.com.mvbos.lgj.base.*;
import br.com.mvbos.lgj.rank.*;

import javax.swing.*;

public class JogoCenario extends CenarioPadrao{

	enum Estado {
		JOGANDO, GANHOU, PERDEU
	}

	private static final int _LARG = 25;

	private static final int RASTRO_INICIAL = 5;

	private int dx, dy;

	private boolean moveu;

	private int temporizador = 0;

	private long timerInicio = 0;

	private long timerFinal = 0;

	private int cronometro = 0;

	private int contadorRastro = RASTRO_INICIAL;

	private Elemento fruta;

	private Elemento serpente;

	private Elemento[] nivel;

	private Elemento[] rastros;

	private Texto texto = new Texto(new Font("Arial", Font.PLAIN, 25));

	private Random rand = new Random();

	// Frutas para finalizar o level
	private int dificuldade = 10;

	private boolean flagRank = false;

	private boolean flagEndAudios = true;

	private boolean flagEatAudio = true;

	private int contadorNivel = 0;

	private Estado estado = Estado.JOGANDO;

	public JogoCenario(int largura, int altura) {
		super(largura, altura);
	}

	public void play(String nomeDoAudio){
		URL url = getClass().getResource(nomeDoAudio+".wav");
		AudioClip audio = Applet.newAudioClip(url);
		audio.play();
	}


	@Override
	public void carregar() {
		int xInicial = 0;
		int yInicial = 0;

		// define direcao inicial
		dx = 1;
		rastros = new Elemento[dificuldade + RASTRO_INICIAL];

		fruta = new Elemento(0, 0, _LARG, _LARG);
		fruta.setCor(Color.RED);


		char[][] nivelSelecionado = Nivel.niveis[Jogo.nivel];
		nivel = new Elemento[nivelSelecionado.length * 2];

		for (int linha = 0; linha < nivelSelecionado.length; linha++) {
			for (int coluna = 1; coluna < nivelSelecionado[0].length; coluna++) {
				if (nivelSelecionado[linha][coluna] == '0') {

					Elemento e = new Elemento();
					e.setAtivo(true);
					e.setCor(Color.LIGHT_GRAY);

					e.setPx(_LARG * coluna);
					e.setPy(_LARG * linha);

					e.setAltura(_LARG);
					e.setLargura(_LARG);

					nivel[contadorNivel++] = e;
				} else if (nivelSelecionado[linha][coluna] == '1') {

					Elemento e = new Elemento();
					e.setAtivo(true);
					e.setCor(Color.blue);

					e.setPx(_LARG * coluna);
					e.setPy(_LARG * linha);

					e.setAltura(_LARG);
					e.setLargura(_LARG);

					nivel[contadorNivel++] = e;
				} else if (nivelSelecionado[linha][coluna] == '2') {
					xInicial = linha;
					yInicial = coluna;
				}
			}
		}


		//Define a serpente

		serpente = new Elemento(0, 0, _LARG, _LARG);
		serpente.setPx(xInicial * _LARG);
		serpente.setPy(yInicial * _LARG);
		serpente.setAtivo(true);
		serpente.setCor(Color.YELLOW);
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
		fruta = null;
		rastros = null;
		serpente = null;
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

			serpente.setPx(serpente.getPx() + _LARG * dx);
			serpente.setPy(serpente.getPy() + _LARG * dy);

			if (Util.saiu(serpente, largura, altura)) {
				serpente.setAtivo(false);
				estado = Estado.PERDEU;

			} else {

				// colisao com cenario
				for (int i = 0; i < contadorNivel; i++) {
					if (Util.colide(serpente, nivel[i])) {
						if(nivel[i].getCor() == Color.blue) {
							for (int j = 0; j < contadorNivel; j++)
								if (nivel[j].getCor() == Color.blue && j != i) {
									serpente.setPx(nivel[j].getPx() + dx*_LARG);
									serpente.setPy(nivel[j].getPy() + dy*_LARG);
									break;
								}
						}else {
							serpente.setAtivo(false);
							estado = Estado.PERDEU;
							break;
						}
					}
				}

				// colisao com o rastro
				for (int i = 0; i < contadorRastro; i++) {
					if (Util.colide(serpente, rastros[i])) {
						serpente.setAtivo(false);
						estado = Estado.PERDEU;
						break;
					}
				}
			}

			if (Util.colide(fruta, serpente)) {
				// Adiciona uma pausa
				if (flagEatAudio) {
					play("eatFx");
					flagEatAudio = false;
				}
				temporizador = -10;
				contadorRastro++;
				fruta.setAtivo(false);

				if (contadorRastro == rastros.length) {
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

		} else {
			temporizador += serpente.getVel();
		}

		// Adicionando frutas
		if (estado == Estado.JOGANDO && !fruta.isAtivo()) {
			int x = rand.nextInt(largura / _LARG);
			int y = rand.nextInt(altura / _LARG);

			fruta.setPx(x * _LARG);
			fruta.setPy(y * _LARG);
			fruta.setAtivo(true);

			// colisao com a serpente
			if (Util.colide(fruta, serpente)) {
				fruta.setAtivo(false);
				return;
			}

			// colisao com rastro
			for (int i = 0; i < contadorRastro; i++) {
				if (Util.colide(fruta, rastros[i])) {
					fruta.setAtivo(false);
					return;
				}
			}

			// colisao com cenario
			for (int i = 0; i < contadorNivel; i++) {
				if (Util.colide(fruta, nivel[i])) {
					fruta.setAtivo(false);
					return;
				}
			}
			flagEatAudio = true;
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

						//criando painel para receber dados do player

						JFrame frame = new JFrame("K19 - Java OO");
						frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

						JPanel panel = new JPanel();
						JLabel rotulo = new JLabel();

						rotulo.setText("Nome: ");
						panel.add(rotulo);
						JTextField textField = new JTextField(3);
						panel.add(textField);

						JButton button1 = new JButton("SIM");

						//configurando os butoes

						button1.addActionListener(
								new ActionListener() {
									public void actionPerformed( ActionEvent event ) {
										boolean contineoLoop1 = true;
										do{
										 try{
										 	//recebendo dados

											if (textField.getText().length() == 0)
												textField.setText("***");
											if (textField.getText().length() > 3)
												textField.setText(textField.getText().substring(0, 3));
											player o = new player(textField.getText(), String.valueOf(rastros.length - contadorRastro), String.valueOf(cronometro));

											//fechando as opereçoes

											rank.atualizaRank(o,Jogo.nivel);
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


	@Override
	public void desenhar(Graphics2D g) {

		if (fruta.isAtivo()) {
			fruta.desenha(g);
		}

		for (Elemento e : nivel) {
			if (e == null)
				break;

			e.desenha(g);
		}

		for (int i = 0; i < contadorRastro; i++) {
			rastros[i].desenha(g);
		}

		serpente.desenha(g);

		timerFinal = System.currentTimeMillis();
		if (timerFinal - timerInicio >= 1000 && estado == Estado.JOGANDO) {
			timerInicio = System.currentTimeMillis();
			cronometro += 1;
		}
		texto.desenha(g, "Tempo: " + String.valueOf(cronometro), 10, altura);

		texto.desenha(g, String.valueOf(rastros.length - contadorRastro), largura - 35, altura);

		if (estado != Estado.JOGANDO) {

			if (estado == Estado.GANHOU) {
				texto.desenha(g, "Ganhou!", 180, 180);
				if (flagEndAudios) {
					play("winFx");
					flagEndAudios = false;
				}
			} else {
				texto.desenha(g, "Vixe!", 180, 180);
				if (flagEndAudios) {
					play("Game Over");
					flagEndAudios = false;
				}
			}


			if (Jogo.pausado)
				Jogo.textoPausa.desenha(g, "PAUSA", largura / 2 - Jogo.textoPausa.getFonte().getSize(), altura / 2);
		}

	}
}

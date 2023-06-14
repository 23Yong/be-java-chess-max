package chess.board;

import chess.exception.BusinessException;
import chess.exception.ErrorCode;
import chess.pieces.*;
import chess.pieces.color.Color;
import chess.pieces.type.Type;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class BoardTest {

	private Board sut;

	@BeforeEach
	void initBoard() {
		sut = new Board();
		sut.initialize();
	}

	@DisplayName("기물의 개수를 셀 때 기물의 타입과 색깔이 주어지면 해당 기물의 개수가 반환된다.")
	@MethodSource("provideTypeAndColor")
	@ParameterizedTest
	void givenPieceTypeAndColor_whenCountPieces_thenReturnsCountOfPieces(Type type, Color color, int expectedCount) {
		// given

		// when
		int count = sut.countPieces(type, color);

		// then
		assertThat(count).isEqualTo(expectedCount);
	}

	private static Stream<Arguments> provideTypeAndColor() {
		return Stream.of(
				Arguments.of(Type.PAWN, Color.BLACK, 8),
				Arguments.of(Type.PAWN, Color.WHITE, 8),
				Arguments.of(Type.ROOK, Color.BLACK, 2),
				Arguments.of(Type.ROOK, Color.WHITE, 2),
				Arguments.of(Type.KNIGHT, Color.BLACK, 2),
				Arguments.of(Type.KNIGHT, Color.WHITE, 2),
				Arguments.of(Type.BISHOP, Color.BLACK, 2),
				Arguments.of(Type.BISHOP, Color.WHITE, 2),
				Arguments.of(Type.QUEEN, Color.BLACK, 1),
				Arguments.of(Type.QUEEN, Color.WHITE, 1),
				Arguments.of(Type.KING, Color.BLACK, 1),
				Arguments.of(Type.KING, Color.WHITE, 1)
		);
	}

	@DisplayName("기물을 찾을 때 위치정보가 문자열로 주어지면 해당 위치의 기물을 반환한다.")
	@Test
	void givenPosition_whenFindPiece_thenReturnsPiece() {
		// given
		Position position = new Position("b8");

		// when
		Piece piece = sut.findPiece(position);

		// then
		SoftAssertions.assertSoftly(softAssertions -> {
			softAssertions.assertThat(piece.getColor()).isEqualTo(Color.BLACK);
			softAssertions.assertThat(piece.getType()).isEqualTo(Type.KNIGHT);
		});
	}

	@DisplayName("기물을 찾을 때 올바르지 않은 위치정보가 문자열로 주어지면 예외를 던진다.")
	@ValueSource(strings = {"i1", "f0", "d9"})
	@ParameterizedTest
	void givenInvalidPosition_whenFindPiece_thenThrowsException(String position) {
		// given

		// when & then
		assertThatThrownBy(() -> sut.findPiece(new Position(position)))
				.isInstanceOf(BusinessException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_POSITION);
	}

	@DisplayName("체스판 위에 기물을 배치할 때 임의의 기물이 주어지면 배치에 성공한다.")
	@Test
	void givenPiece_whenPlacePiece_thenSuccessPlacePiece() {
		// given
		Pawn pawn = Pawn.of(Color.BLACK);

		// when
		sut.placePiece(pawn, new Position("a3"));

		// then
		Piece piece = sut.findPiece(new Position("a3"));
		SoftAssertions.assertSoftly(softAssertions -> {
			softAssertions.assertThat(piece.getType()).isEqualTo(Type.PAWN);
			softAssertions.assertThat(piece.getColor()).isEqualTo(Color.BLACK);
		});
	}

	@DisplayName("포인트를 계산할 때 임의의 초기화된 체스판이 주어지면 총 점수를 반환한다.")
	@Test
	void givenInitializedBoard_whenCalculatePoint_thenReturnsTotalPoint() {
		// given
		initializeChessBoard();

		// when
		float blackPoint = sut.calculatePoint(Color.BLACK);
		float whitePoint = sut.calculatePoint(Color.WHITE);

		// then
		SoftAssertions.assertSoftly(softAssertions -> {
			softAssertions.assertThat(blackPoint).isEqualTo(15.0F);
			softAssertions.assertThat(whitePoint).isEqualTo(19.5F);
		});
	}

	private void initializeChessBoard() {
		sut.initializeEmpty();

		sut.placePiece(Pawn.of(Color.BLACK), new Position("b6"));
		sut.placePiece(Queen.of(Color.BLACK), new Position("e6"));
		sut.placePiece(King.of(Color.BLACK), new Position("b8"));
		sut.placePiece(Rook.of(Color.BLACK), new Position("c8"));

		sut.placePiece(Knight.of(Color.WHITE), new Position("f4"));
		sut.placePiece(Queen.of(Color.WHITE), new Position("g4"));
		sut.placePiece(Pawn.of(Color.WHITE), new Position("f2"));
		sut.placePiece(Pawn.of(Color.WHITE), new Position("f3"));
		sut.placePiece(Pawn.of(Color.WHITE), new Position("h3"));
		sut.placePiece(Pawn.of(Color.WHITE), new Position("g2"));
		sut.placePiece(Rook.of(Color.WHITE), new Position("e1"));
		sut.placePiece(King.of(Color.WHITE), new Position("f1"));
	}

	@DisplayName("턴이 유효한지 확인할 때 현재 턴과 움직이려는 위치 정보가 주어지면 확인에 성공한다.")
	@Test
	void givenCurrentTurnAndPosition_whenCheckTurn_thenSuccessCheck() {
		// given
		Color currentTurn = Color.WHITE;
		Position position = new Position("a2");

		// when & then
		assertThatCode(() -> sut.checkTurn(currentTurn, position))
				.doesNotThrowAnyException();
	}

	@DisplayName("턴이 유효한지 확인할 때 현재 턴과 움직이려는 위치 정보가 일치하지 않으면 예외를 던진다.")
	@Test
	void givenCurrentTurnAndInvalidPosition_whenCheckTurn_thenThrowsException() {
		// given
		Color currentTurn = Color.BLACK;
		Position position = new Position("a2");

		// when & then
		assertThatThrownBy(() -> sut.checkTurn(currentTurn, position))
				.isInstanceOf(BusinessException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_TURN);
	}
}

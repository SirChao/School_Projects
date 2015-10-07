from engines import Engine
from copy import deepcopy

class StudentEngine(Engine):
    """ Game engine that implements a simple fitness function maximizing the
    difference in number of pieces in the given color's favor. """
    def __init__(self):
        self.alpha_beta = False

    def get_move(self, board, color, move_num=None,
                 time_remaining=None, time_opponent=None):
        """ Return a move for the given color that maximizes the difference in 
        number of pieces for that color. """
        # Get a list of all legal moves.
        moves = board.get_legal_moves(color)

        # Return the best move according to our simple utility function:
        # which move yields the largest different in number of pieces for the
        # given color vs. the opponent?
	if(self.alpha_beta == True):
	    move = self.bestMoveAlphaBeta(board, 2, color, float("-inf"), float("inf"))
	else:
	    move = self.bestMove(board, 2, color)
	return move[1]
	"""return max(moves, key=lambda move: self._get_cost(board, color, move))"""
    
    def bestMove(self, posn, depth, color):
	if(depth == 0):
	    return [self.Evaluate(posn, color), None]
	moveList = posn.get_legal_moves(color)
	bestScore = float("-inf")
	bestm = None
	
	while(len(moveList) > 0):
	    tempTry = self.bestMove(self.NewPosition(posn, moveList[0], color, depth), depth-1, color)
	    tryScore = -tempTry[0]
	    
	    if(tryScore > bestScore):
		bestScore = tryScore
		bestm = moveList[0]
	    
	    moveList = moveList[1:]
	
	return([bestScore, bestm])

    def Evaluate(self, posn, color):
	score = 0
	newboard = deepcopy(posn)
	opPieces = newboard.get_squares(color * -1)
	myPieces = newboard.get_squares(color)
	num_pieces_op = len(opPieces)
	num_pieces_me = len(myPieces)
	score = num_pieces_me - num_pieces_op
	for i in range(0,len(opPieces)):
	    for n in range (1,6):
		if(opPieces[i] == (n, 0)or opPieces[i] == (0,n) or opPieces[i] == (n,7) or opPieces[i] == (7,n)):
		    score = score - 2;
	    if(opPieces[i] == (0,0) or opPieces[i] == (7,7) or opPieces[i] == (0,7) or opPieces[i] == (7,0)):
		score = score - 4;
	
	for i in range(0,len(myPieces)):
	    for n in range (1,6):
		if(myPieces[i] == (n, 0)or myPieces[i] == (0,n) or myPieces[i] == (n,7) or myPieces[i] == (7,n)):
		    score = score + 2;
	    if(myPieces[i] == (0,0) or myPieces[i] == (7,7) or myPieces[i] == (0,7) or myPieces[i] == (7,0)):
		score = score + 4;
	print score
	return score
    
    def NewPosition(self, posn, move, color, depth):
	newboard = deepcopy(posn)
	newboard.execute_move(move, color)
	
	opponentMoveList = self.bestMove(newboard, depth-1, color * -1)
	opponentMove = opponentMoveList[1]
	
	if(opponentMove != None):
	    newboard.execute_move(opponentMove, color * -1)
	return newboard

    def bestMoveAlphaBeta(self, posn, depth, color, myBest, opBest):
	if(depth == 0):
	    return [self.Evaluate(posn, color), None]
	moveList = posn.get_legal_moves(color)
	bestScore = myBest
	bestm = None
	
	while(len(moveList) > 0):
	    tempTry = self.bestMoveAlphaBeta(self.NewPosition(posn, moveList[0], color, depth), depth-1, color, -opBest, -bestScore)
	    tryScore = -tempTry[0]
	    
	    if(tryScore > bestScore):
		bestScore = tryScore
		bestm = moveList[0]
	    if(bestScore > opBest):
		return ([bestScore, bestm])
	    
	    moveList = moveList[1:]
	
	return([bestScore, bestm])

engine = StudentEngine

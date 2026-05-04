package main

import (
	"fmt"
	_ "fmt"
	"math/rand/v2"
	_ "math/rand/v2"
	"time"
	_ "time"
)

func match_agent(player1In, player2In <-chan int, winnerOut chan<- int, matchName string) {
	p1 := <-player1In
	p2 := <-player2In

	winner := p1
	loser := p2
	if rand.IntN(2) == 1 {
		winner = p2
		loser = p1
	}

	fmt.Printf("[%s] Player %d beat Player %d\n", matchName, winner, loser)

	winnerOut <- winner
}

func main() {
	m := 10
	numPlayers := 1 << m

	fmt.Print("Starting Challenge. Number of players: ", numPlayers)
	start := time.Now()
	var currentLayer []chan int
	for i := 0; i < numPlayers; i++ {
		ch := make(chan int, 1)
		ch <- i
		currentLayer = append(currentLayer, ch)
	}

	roundNum := 1
	for len(currentLayer) > 1 {
		var nextLayer []chan int

		// Take players in pairs
		for i := 0; i < len(currentLayer); i += 2 {
			outCh := make(chan int)
			matchName := fmt.Sprintf("Round %d, Game %d", roundNum, (i/2)+1)

			go match_agent(currentLayer[i], currentLayer[i+1], outCh, matchName)

			nextLayer = append(nextLayer, outCh)
		}
		roundNum++
		currentLayer = nextLayer
	}

	champion := <-currentLayer[0]

	end := time.Now()
	delta := end.Sub(start)
	fmt.Printf("\n🏆 The Champion is Player %d! 🏆\n", champion)
	fmt.Printf("Time passed: %v\n", delta)
}
